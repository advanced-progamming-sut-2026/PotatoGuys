package com.pvz.models.entities.plants;

import java.util.List;

import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.plants.data.GrowthProfile;
import com.pvz.models.entities.plants.data.PlantFoodProfile;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.plants.fsm.PlantDeadState;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.IceBlockBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class Plant extends Entity {

    public static final int TICKS_PER_SECOND = 10;

    private final PlantPropertySheet sheet;
    private final GameContext context;

    private final int col;
    private final int lane;
    private int level;
    private boolean boosted;
    private int boostedTicksRemaining;

    private ResolvedStats stats;
    private final GrowthProfile growth;

    private float hp;
    private float maxHpBonus;
    private float ageTicks;
    private int growthStageIndex;

    private PlantState currentState;
    private final PlantAction attackAction;
    private final PlantAction feedAction;
    private boolean dead;

    private int freezeLevel = 0; // 0, 1, 2, 3
    private boolean isFrozen = false;
    private float iceHp = 0f;
    private static final float MAX_ICE_HP = 600f;


    public Plant(PlantPropertySheet sheet, PlantAction attackAction, PlantAction feedAction, int col, int lane,
            int level, boolean boosted, GameContext context) {
        this.sheet = sheet;
        this.attackAction = attackAction;
        this.feedAction = feedAction;
        this.col = col;
        this.lane = lane;
        this.level = Math.max(1, Math.min(4, level));
        this.boosted = boosted;
        this.context = context;
        this.stats = PlantStatResolver.resolve(sheet, this.level);
        this.growth = sheet.getGrowth();
        this.hp = stats.getMaxHp();
        this.position.set(GameController.colToWorldX(col), GameController.laneToWorldY(lane));
        setHitbox(Tile.WIDTH, Tile.HEIGHT);
    }

    // ── TickAware ─────────────────────────────────────────────────────────────

    @Override
    public void enter() {
        if (currentState == null) currentState = new PlantIdleState();
        currentState.onEnter(this, context);
        context.log("[Plant] " + sheet.getName() + " planted at (" + col + "," + lane + ")"
                + (level > 1 ? " [Lvl " + level + "]" : ""));
    }

    @Override
    public void update(float dt) {
        if (dead || isFrozen)
            return;
        syncHitbox();
        tickGrowth();
        tickBoost();
        if (this.currentState != null) {
            currentState.update(this, context , dt);
        }else{
            changeState(new PlantIdleState());
        }
    }

    @Override
    public void dispose() {
        context.removePlant(this);
    }

    @Override
    public FrameConfig draw(){
        if(currentState != null){
            return currentState.draw(this, context);
        }
        return null;
    }

    public void changeState(PlantState nextState){
        if(currentState != null){
            currentState.onExit(this, context);
        }
        currentState = nextState;
        currentState.onEnter(this, context);
    }

    // ── Cold Wind / Freezing ──────────────────────────────────────────────────

    public void incrementFreezeLevel() {
        if (isFrozen)
            return;
        freezeLevel++;
        if (freezeLevel >= 3) {
            isFrozen = true;
            iceHp = MAX_ICE_HP;
            Tile tile = context.getTileAt(col, lane);
            if (!tile.getTags().contains(TileTags.ICE_BLOCK))
                tile.getTags().add(TileTags.ICE_BLOCK);
            context.getTileAt(col, lane).addBehavior(new IceBlockBehavior(this));
            context.log("[Freeze] " + sheet.getName() + " is frozen!");
        }
    }

    public void takeIceDamage(float amount, boolean isFire) {
        if (!isFrozen)
            return;
        if (isFire) {
            iceHp = 0f;
        } else {
            iceHp = Math.max(0f, iceHp - amount);
        }

        if (iceHp <= 0f) {
            isFrozen = false;
            freezeLevel = 0;
            context.log("[ColdWind] Ice on " + sheet.getName() + " melted!");
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public float getIceHp() {
        return iceHp;
    }

    // ── Damage / health ────────────────────────────────────────────────────────

    /**
     * Plants have no armor layer (rule: any "armor" text just adjusts HP), so
     * damage is direct.
     */
    public void takeDamage(float amount, DamageKind kind) {
        if (dead || amount <= 0f)
            return;

        // If frozen, ice takes damage first
        if (isFrozen) {
            boolean isFire = (kind == DamageKind.FIRE); // Need to define/check Fire damage
            takeIceDamage(amount, isFire);
            return; // Ice absorbed the damage
        }

        hp = Math.max(0f, hp - amount);
        if (hp <= 0f)
            kill();
    }

    /**
     * Permanently raises max HP (and current HP) — used by armor-flavored Plant
     * Food effects.
     */
    public void boostMaxHp(float amount) {
        if (amount <= 0f)
            return;
        maxHpBonus += amount;
        hp += amount;
    }

    public void kill() {
        if (dead)
            return;
        dead = true;
        currentState = new PlantDeadState();
        context.removePlant(this);
        context.log("[Plant] " + sheet.getName() + " at (" + col + "," + lane + ") was destroyed.");
    }

    public void heal(float amount) {
        hp = Math.min(getMaxHp(), hp + Math.max(0, amount));
    }

    // ── Plant Food ────────────────────────────────────────────────────────────
    public void triggerPlantFood(GameContext ctx) {
        if (feedAction == null) {
            ctx.log("[PlantFood] " + sheet.getName() + " has no Plant Food effect!");
            return;
        }
        changeState(feedAction);
        ctx.log("[PlantFood] " + sheet.getName() + " used its Plant Food!");
    }

    // ── Growth (wramp-up plants) ───────────────────────────────────────────────

    private void tickGrowth() {
        ageTicks++;
        if (growth != null) {
            growthStageIndex = growth.stageIndexFor(ageTicks / TICKS_PER_SECOND);
        }
    }

    private void tickBoost() {
        if (boostedTicksRemaining <= 0)
            return;
        boostedTicksRemaining--;
        if (boostedTicksRemaining <= 0) {
            boosted = false;
            context.log("[PlantFood] " + sheet.getName() + " at (" + col + "," + lane + ") boost wore off.");
        }
    }


    public float getEffectiveDamage() {
        float base = sheet.getDamage().getKind() == DamageKind.STAGED && growth != null
                ? sheet.getDamage().valueAtStage(growthStageIndex)
                : sheet.getDamage().getValue();
        float levelDelta = stats.getDamage() - sheet.getDamage().getValue();
        return base + levelDelta;
    }


    // ── Accessors ─────────────────────────────────────────────────────────────

    public PlantPropertySheet getSheet() {
        return sheet;
    }

    public GameContext getContext() {
        return context;
    }

    public PlantAction getAttackAction() {
        return attackAction;
    }

    public PlantAction getFeedAction() {
        return feedAction;
    }

    public PlantType getType() {
        return sheet.getType();
    }

    public int getCol() {
        return col;
    }

    public int getLane() {
        return lane;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public int getLevel() {
        return level;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
    }

    /**
     * Re-resolves every level-scaled stat (mirrors leveling up a card in the
     * Collection UI).
     */
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(4, level));
        this.stats = PlantStatResolver.resolve(sheet, this.level);
        this.hp = getMaxHp();
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return stats.getMaxHp() + maxHpBonus;
    }

    public int getSunCost() {
        return stats.getSunCost();
    }

    public Float getActionIntervalSeconds() {
        return stats.getActionIntervalSeconds();
    }

    public Float getRechargeSeconds() {
        return stats.getRechargeSeconds();
    }

    public int getRangeTiles() {
        return stats.getRangeTiles();
    }

    public int getPierceCount() {
        return stats.getPierceCount();
    }

    public float getEffectDurationBonusSeconds() {
        return stats.getEffectDurationBonusSeconds();
    }

    public List<String> getUnlockedFlags() {
        return stats.getUnlockedFlags();
    }

    public int getGrowthStageIndex() {
        return growthStageIndex;
    }

    public boolean isDead() {
        return dead;
    }

    public PlantState getCurrentState() {
        return currentState;
    }

    public boolean isPlantableOnTile(Tile tile){
        return true;
    }

    // ── CLI display ────────────────────────────────────────────────────────────

    public String toInfoString() {
        return sheet.getName() + " (" + sheet.getCategory() + ")"
                + "\n  position: " + col + ", " + lane
                + "\n  health: " + String.format("%.0f", hp) + " / " + String.format("%.0f", getMaxHp())
                + "\n  level: " + level
                + "\n  state: " + (currentState != null ? currentState.getLabel() : "?");
    }
}
