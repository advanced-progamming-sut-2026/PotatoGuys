package pvz.models.entities.plants;

import java.util.List;

import pvz.models.engine.TickAware;
import pvz.models.entities.plants.actions.PlantAction;
import pvz.models.entities.plants.data.DamageKind;
import pvz.models.entities.plants.data.GrowthProfile;
import pvz.models.entities.plants.data.PlantFoodExecutor;
import pvz.models.entities.plants.data.PlantFoodProfile;
import pvz.models.entities.plants.data.PlantPropertySheet;
import pvz.models.entities.plants.data.PlantStatResolver;
import pvz.models.entities.plants.data.ProductionKind;
import pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import pvz.models.entities.plants.fsm.PlantIdleState;
import pvz.models.entities.plants.fsm.PlantState;
import pvz.models.games.GameContext;
import pvz.models.games.map.behaviors.IceBlockBehavior;
import pvz.models.games.map.tile.Tile;
import pvz.models.games.map.tile.TileTags;

/**
 * Concrete, data-driven plant entity.
 *
 * <p>All 69 plant kinds share this single class. Unique behavior comes from:
 * <ol>
 *   <li>A {@link PlantPropertySheet} — immutable parsed stats loaded from JSON.</li>
 *   <li>A resolved {@link ResolvedStats} snapshot — base stats plus every level
 *       upgrade unlocked at {@link #level}, computed once at construction
 *       (mirrors {@code Zombie}'s wave-scaling, generalized to plant leveling).</li>
 *   <li>A single {@link PlantAction} plug-in — the Strategy-pattern behavior
 *       (shoot / produce sun / explode / buff family / ...) chosen by
 *       {@link PlantFactory} from the sheet's category.</li>
 * </ol>
 *
 * <h3>Tick lifecycle</h3>
 * <pre>
 *   enter()      – set initial PlantIdleState, log placement
 *   update() × N – tick FSM (cooldown countdown -&gt; action -&gt; idle) + growth timer
 *   dispose()    – cleanup on engine removal
 * </pre>
 *
 * <p>Plants are stationary, so unlike {@code Zombie} there is no float x /
 * velocity — just an integer (col, lane) grid cell.
 */
public class Plant implements TickAware {

    /** Matches {@code Zombie.TICKS_PER_SECOND} so both systems share one clock. */
    public static final int TICKS_PER_SECOND = 10;

    private final PlantPropertySheet sheet;
    private final GameContext context;
    private final PlantAction action;

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
    private boolean dead;

    private int freezeLevel = 0; // 0, 1, 2, 3
    private boolean isFrozen = false;
    private float iceHp = 0f;
    private static final float MAX_ICE_HP = 600f;

    /**
     * @param context world adapter, or {@code null} for an "unplaced" record
     *                (e.g. a catalog/collection entry that is never registered
     *                with a {@link pvz.models.engine.GameEngine} and never has
     *                {@link #enter()}/{@link #update()} invoked)
     */
    public Plant(PlantPropertySheet sheet, PlantAction action, int col, int lane,
                 int level, boolean boosted, GameContext context) {
        this.sheet = sheet;
        this.action = action;
        this.col = col;
        this.lane = lane;
        this.level = Math.max(1, Math.min(4, level));
        this.boosted = boosted;
        this.context = context;
        this.stats = PlantStatResolver.resolve(sheet, this.level);
        this.growth = sheet.getGrowth();
        this.hp = stats.getMaxHp();
    }

    // ── TickAware ─────────────────────────────────────────────────────────────

    @Override
    public void enter() {
        currentState = new PlantIdleState();
        currentState.onEnter(this, context);
        context.log("[Plant] " + sheet.getName() + " planted at (" + col + "," + lane + ")"
                + (level > 1 ? " [Lvl " + level + "]" : ""));
    }

    @Override
    public void update() {
        if (dead || isFrozen) return;
        tickGrowth();
        tickBoost();
        PlantState next;
        if (this.currentState != null) {
            next = currentState.tick(this, context);
        } else {
            this.currentState = new PlantIdleState(); 
            next = currentState.tick(this, context);
        }
        if (next != currentState) {
            currentState.onExit(this, context);
            next.onEnter(this, context);
            currentState = next;
        }
    }

    @Override
    public void dispose() {
        // no owned resources to release
    }

    // ── Cold Wind / Freezing ──────────────────────────────────────────────────

    public void incrementFreezeLevel() {
        if (isFrozen) return;
        freezeLevel++;
        if (freezeLevel >= 3) {
            isFrozen = true;
            iceHp = MAX_ICE_HP;
            Tile tile=context.getTileAt(col,lane);
            if (!tile.getTags().contains(TileTags.ICE_BLOCK)) tile.getTags().add(TileTags.ICE_BLOCK);
            context.getTileAt(col,lane).addBehavior(new IceBlockBehavior(this));
            context.log("[Freeze] " + sheet.getName() + " is frozen!");
        }
    }

    public void takeIceDamage(float amount, boolean isFire) {
        if (!isFrozen) return;
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

    public boolean isFrozen() { return isFrozen; }
    public float getIceHp() { return iceHp; }

    // ── Damage / health ────────────────────────────────────────────────────────

    /** Plants have no armor layer (rule: any "armor" text just adjusts HP), so damage is direct. */
    public void takeDamage(float amount, DamageKind kind) {
        if (dead || amount <= 0f) return;

        // If frozen, ice takes damage first
        if (isFrozen) {
            boolean isFire = (kind == DamageKind.FIRE); // Need to define/check Fire damage
            takeIceDamage(amount, isFire);
            return; // Ice absorbed the damage
        }

        hp = Math.max(0f, hp - amount);
        if (hp <= 0f) kill();
    }


    /** Permanently raises max HP (and current HP) — used by armor-flavored Plant Food effects. */
    public void boostMaxHp(float amount) {
        if (amount <= 0f) return;
        maxHpBonus += amount;
        hp += amount;
    }

    public void kill() {
        if (dead) return;
        dead = true;
        currentState = new pvz.models.entities.plants.fsm.PlantDeadState();
        context.removePlant(this);
        context.log("[Plant] " + sheet.getName() + " at (" + col + "," + lane + ") was destroyed.");
    }

    public void heal(float amount) { hp = Math.min(getMaxHp(), hp + Math.max(0, amount)); }

    // ── Plant Food ────────────────────────────────────────────────────────────

    /** Triggers this plant's own Plant-Food effect immediately. */
    public void  triggerPlantFood(GameContext ctx) {
        PlantFoodProfile pf = sheet.getPlantFood();
        boosted = true;
        boostedTicksRemaining = Math.max(1, Math.round(pf.getDurationSeconds() * TICKS_PER_SECOND));
        PlantFoodExecutor.execute(this, ctx);
        ctx.log("[PlantFood] " + sheet.getName() + " is boosted for " + String.format("%.1f", pf.getDurationSeconds()) + "s!");
    }

    // ── Growth (wramp-up plants) ───────────────────────────────────────────────

    private void tickGrowth() {
        ageTicks++;
        if (growth != null) {
            growthStageIndex = growth.stageIndexFor(ageTicks / TICKS_PER_SECOND);
        }
    }

    private void tickBoost() {
        if (boostedTicksRemaining <= 0) return;
        boostedTicksRemaining--;
        if (boostedTicksRemaining <= 0) {
            boosted = false;
            context.log("[PlantFood] " + sheet.getName() + " at (" + col + "," + lane + ") boost wore off.");
        }
    }

    /** Damage for the plant's current growth stage (flat for non wramp-up plants). */
    public float getEffectiveDamage() {
        float base = sheet.getDamage().getKind() == DamageKind.STAGED && growth != null
                ? sheet.getDamage().valueAtStage(growthStageIndex)
                : sheet.getDamage().getValue();
        float levelDelta = stats.getDamage() - sheet.getDamage().getValue();
        return base + levelDelta;
    }

    /** Sun amount for the plant's current growth stage (flat for non-staged producers). */
    public float getEffectiveProductionAmount() {
        var production = sheet.getProduction();
        if (production == null) return 0f;
        if (production.getKind() == ProductionKind.STAGED) {
            return production.amountAtStage(growthStageIndex);
        }
        return production.getAmount();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public PlantPropertySheet getSheet()   { return sheet; }
    public GameContext getContext()       { return context; }
    public PlantAction getAction()         { return action; }
    public pvz.models.entities.plants.enums.PlantType getType() { return sheet.getType(); }
    public int getCol()                    { return col; }
    public int getLane()                   { return lane; }
    public int getLevel()                  { return level; }
    public boolean isBoosted()             { return boosted; }
    public void setBoosted(boolean boosted){ this.boosted = boosted; }

    /** Re-resolves every level-scaled stat (mirrors leveling up a card in the Collection UI). */
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(4, level));
        this.stats = PlantStatResolver.resolve(sheet, this.level);
        this.hp = getMaxHp();
    }

    public float getHp()                   { return hp; }
    public float getMaxHp()                { return stats.getMaxHp() + maxHpBonus; }
    public int getSunCost()                { return stats.getSunCost(); }
    public Float getActionIntervalSeconds(){ return stats.getActionIntervalSeconds(); }
    public Float getRechargeSeconds()      { return stats.getRechargeSeconds(); }
    public int getRangeTiles()             { return stats.getRangeTiles(); }
    public int getPierceCount()            { return stats.getPierceCount(); }
    public float getEffectDurationBonusSeconds() { return stats.getEffectDurationBonusSeconds(); }
    public List<String> getUnlockedFlags() { return stats.getUnlockedFlags(); }
    public int getGrowthStageIndex()       { return growthStageIndex; }
    public boolean isDead()                { return dead; }
    public PlantState getCurrentState()    { return currentState; }

    // ── CLI display ────────────────────────────────────────────────────────────

    public String toInfoString() {
        return sheet.getName() + " (" + sheet.getCategory() + ")"
                + "\n  position: " + col + ", " + lane
                + "\n  health: " + String.format("%.0f", hp) + " / " + String.format("%.0f", getMaxHp())
                + "\n  level: " + level
                + "\n  state: " + (currentState != null ? currentState.getLabel() : "?");
    }
}
