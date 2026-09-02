package com.pvz.models.entities.plants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.effects.PlantFoodFxEffect;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.actions.SunBeanAction;
import com.pvz.models.entities.plants.actions.TorchwoodAction;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.plants.data.GrowthProfile;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.plants.fsm.PlantDeadState;
import com.pvz.models.entities.plants.fsm.PlantFlashState;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.IceBlockBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class Plant extends Entity {
    private static final String CHILL_PAM_PATH = "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM";
    private static final String CHILL_CLIP_1 = "chill_stage1";
    private static final String CHILL_CLIP_2 = "chill_stage2";

    private static final String FROZEN_PAM = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    private static final String FROZEN_CLIP = "freeze_idle";

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
    private float reflectDamageBonus;
    private int growthStageIndex;

    private float stateTime;

    private PlantState currentState;
    private final PlantAction attackAction;
    private final PlantAction feedAction;
    private boolean dead;

    /** When true the plant is bound by an octopus overlay and cannot act. */
    private boolean bound;

    private float armorHp = 0f;
    private float armorMaxHp = 0f;
    private boolean hasArmor = false;
    private String[] armorPartNames = new String[0];
    private String armorContainerName = null;

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
        if (currentState == null)
            currentState = new PlantIdleState();
        currentState.onEnter(this, context);
        context.log("[Plant] " + sheet.getName() + " planted at (" + col + "," + lane + ")"
                + (level > 1 ? " [Lvl " + level + "]" : ""));
        if (boosted && feedAction != null) {
            boosted = false;
            boostedTicksRemaining = 0;
            changeState(feedAction);
            context.addEffect(new PlantFoodFxEffect(context, position));
            context.log("[PlantFood] " + sheet.getName() + " auto-triggered Plant Food (boosted)!");
        }
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (dead || isFrozen || bound)
            return;
        syncHitbox();
        // tickGrowth();
        tickBoost();
        if (this.currentState != null) {
            currentState.update(this, context, dt);
        } else {
            changeState(new PlantIdleState());
        }
    }

    @Override
    public void dispose() {
        context.removePlant(this);
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        if (currentState != null) {
            frameConfigs.add(currentState.draw(this, context));
        }
        if (freezeLevel == 1) {
            frameConfigs.add(new FrameConfig(CHILL_PAM_PATH, CHILL_CLIP_1, stateTime, position,
                    new Vector2(0.65f, 0.65f), null, false));
        } else if (freezeLevel == 2) {
            frameConfigs.add(new FrameConfig(CHILL_PAM_PATH, CHILL_CLIP_2, stateTime, position,
                    new Vector2(0.65f, 0.65f), null, false));
        }
        if (isFrozen) {
            Vector2 scale = new Vector2(0.65f, 0.65f);
            FrameConfig iceBlock = new FrameConfig(FROZEN_PAM, FROZEN_CLIP, stateTime, position, scale, null, false);
            iceBlock.setColor(1, 1, 1, 0.45f);
            frameConfigs.add(iceBlock);
        }
        return frameConfigs;
    }

    public void changeState(PlantState nextState) {
        if (currentState != null) {
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
            tile.addBehavior(new IceBlockBehavior(tile, this));
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

        // Sun Bean: every zombie bite drops one sun onto the plant's own tile.
        if (attackAction instanceof SunBeanAction sunBean) {
            sunBean.spawnHitSun(this, context);
        }

        if (isFrozen) {
            boolean isFire = (kind == DamageKind.FIRE);
            takeIceDamage(amount, isFire);
            return;
        }

        if (hasArmor && armorHp > 0f) {
            armorHp = Math.max(0f, armorHp - amount);
            if (armorHp <= 0f) {
                hasArmor = false;
            }
            if (currentState != null && !(currentState instanceof PlantDeadState)
                    && !(currentState instanceof PlantFlashState)) {
                currentState = new PlantFlashState(currentState);
            }
            return;
        }

        hp = Math.max(0f, hp - amount);
        if (currentState != null && !(currentState instanceof PlantDeadState)
                && !(currentState instanceof PlantFlashState)) {
            currentState = new PlantFlashState(currentState);
        }
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
        if (currentState != null)
            currentState.onExit(this, context);
        if (attackAction instanceof TorchwoodAction torchwood) {
            torchwood.onDeath(this, context);
        }
        currentState = new PlantDeadState();
        context.getGameStats().onPlantLost();
        context.removePlant(this);
        context.log("[Plant] " + sheet.getName() + " at (" + col + "," + lane + ") was destroyed.");
    }

    public void heal(float amount) {
        hp = Math.min(getMaxHp(), hp + Math.max(0, amount));
    }

    /**
     * Permanently raises the plant's reflect-damage bonus (Endurian Plant Food).
     */
    public void addReflectDamageBonus(float amount) {
        if (amount <= 0f)
            return;
        reflectDamageBonus += amount;
    }

    public float getReflectDamageBonus() {
        return reflectDamageBonus;
    }

    // ── Plant Food ────────────────────────────────────────────────────────────
    public void triggerPlantFood(GameContext ctx) {
        if (feedAction == null) {
            ctx.log("[PlantFood] " + sheet.getName() + " has no Plant Food effect!");
            return;
        }
        changeState(feedAction);
        ctx.addEffect(new PlantFoodFxEffect(ctx, position));
        ctx.log("[PlantFood] " + sheet.getName() + " used its Plant Food!");
    }

    // ── Growth (wramp-up plants) ───────────────────────────────────────────────

    /*
     * private void tickGrowth() {
     * ageTicks++;
     * if (growth != null) {
     * growthStageIndex = growth.stageIndexFor(ageTicks / TICKS_PER_SECOND);
     * }
     * }
     */

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

    public void setHp(float hp) {
        this.hp = hp;
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

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    // ── Armor ────────────────────────────────────────────────────────────────

    public void setArmor(float maxHp, String[] partNames, String containerName) {
        this.armorMaxHp = maxHp;
        this.armorHp = maxHp;
        this.hasArmor = true;
        this.armorPartNames = partNames != null ? partNames : new String[0];
        this.armorContainerName = containerName;
    }

    public boolean hasArmor() {
        return hasArmor;
    }

    public float getArmorHp() {
        return armorHp;
    }

    public float getArmorMaxHp() {
        return armorMaxHp;
    }

    public String[] getArmorPartNames() {
        return armorPartNames;
    }

    /**
     * Returns the armor part name to show based on armor HP fraction.
     * index 0 = full, 1 = 1/3 gone, 2 = 2/3 gone.
     */
    public String getVisibleArmorPart() {
        if (!hasArmor || armorPartNames.length == 0)
            return null;
        float fraction = armorMaxHp <= 0f ? 1f : armorHp / armorMaxHp;
        if (fraction > 0.66f) {
            return armorPartNames[0];
        } else if (fraction > 0.33f) {
            return armorPartNames.length > 1 ? armorPartNames[1] : armorPartNames[0];
        } else {
            return armorPartNames.length > 2 ? armorPartNames[2] : armorPartNames[armorPartNames.length - 1];
        }
    }

    /**
     * Returns the partsVisibility map for the current armor state, or {@code null}
     * if no armor is active. Every state's {@code draw()} should pass this directly
     * into {@link FrameConfig#FrameConfig}.
     */
    public Map<String, Boolean> getArmorPartsVisibility() {
        if (!hasArmor || armorPartNames.length == 0)
            return null;
        String visiblePart = getVisibleArmorPart();
        Map<String, Boolean> map = new HashMap<>();
        if (armorContainerName != null) {
            map.put(armorContainerName, true);
        }
        for (String partName : armorPartNames) {
            map.put(partName, partName.equals(visiblePart));
        }
        return map;
    }

    public PlantState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(PlantState state) {
        this.currentState = state;
    }

    public boolean isPlantableOnTile(Tile tile) {
        return true;
    }
}
