package pvz.Models.Entities.Zombies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Zombies.armor.ArmorFlag;
import pvz.Models.Entities.Zombies.armor.ArmorPiece;
import pvz.Models.Entities.Zombies.data.ScaledProp;
import pvz.Models.Entities.Zombies.data.ZombiePropertySheet;
import pvz.Models.Entities.Zombies.effects.EffectType;
import pvz.Models.Entities.Zombies.effects.StatusEffect;
import pvz.Models.Entities.Zombies.fsm.DeadState;
import pvz.Models.Entities.Zombies.fsm.WalkState;
import pvz.Models.Entities.Zombies.fsm.ZombieState;
import pvz.Models.Entities.Zombies.skills.ZombieSkill;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.map.Tile;
import pvz.Models.Games.map.behaviors.TileBehavior;


/**
 * Concrete, data-driven zombie entity.
 *
 * <p>All 52+ zombie types share this single class. Unique behaviour comes from:
 * <ol>
 *   <li>A {@link ZombiePropertySheet} — immutable parsed stats.</li>
 *   <li>An ordered {@link ArmorPiece} stack — layered HP shield.</li>
 *   <li>A {@link ZombieSkill} list — special ability plug-ins.</li>
 * </ol>
 *
 * <h3>Tick lifecycle</h3>
 * <pre>
 *   enter()          – set initial WalkState, log spawn
 *   update() × N     – tick FSM + status effects (10 ticks = 1 second)
 *   dispose()        – cleanup on engine removal
 * </pre>
 *
 * <h3>Coordinate system</h3>
 * Column 0 = house side, column (cols-1) = spawn side.
 * {@code x} is a {@code float}; integer cell = {@code (int) x}.
 * Each tick {@code x} decreases by {@link #getEffectiveSpeedPerTick()}.
 */
public class Zombie implements TickAware {

    /**
     * 10 ticks equal one in-game second (project spec §Mekanism Gozar Zaman).
     * All per-second values (DPS, cooldowns, speed) are divided by this constant.
     */
    public static final int TICKS_PER_SECOND = 10;

    private static final Random RNG = new Random();
    private static final float GLOW_CHANCE = 0.05f;

    // ── Identity ──────────────────────────────────────────────────────────────
    private final ZombiePropertySheet sheet;
    private final GameContext context;

    // ── Grid position ─────────────────────────────────────────────────────────
    private int lastX;
    private int lastLane;
    private float x;
    private final int lane;

    // ── Runtime stats (scaled at spawn) ───────────────────────────────────────
    private final float maxHp;
    private float hp;
    private final float eatDpsPerTick;
    private final float speedPerTick;

    // ── Components ────────────────────────────────────────────────────────────
    private final List<ArmorPiece> armors;
    private final Map<EffectType, StatusEffect> activeEffects;
    private final List<ZombieSkill> skills;

    // ── FSM ───────────────────────────────────────────────────────────────────
    private ZombieState currentState;

    // ── Flags ─────────────────────────────────────────────────────────────────
    private boolean dead;
    private final boolean glowing;
    private boolean impAlreadyThrown;
    private int stolenSun;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Constructed exclusively by {@link ZombieFactory}.
     *
     * @param sheet      immutable data sheet
     * @param startX     spawn column (typically rightmost)
     * @param lane       row index [0, lanes)
     * @param armors     front-to-back armour stack
     * @param skills     special-ability plug-ins
     * @param ctx        world interface
     * @param waveIndex  0-based wave number for ScaledProp scaling
     * @param difficulty [1..5]; 3 = no modifier
     */
    public Zombie(ZombiePropertySheet sheet, float startX, int lane,
                  List<ArmorPiece> armors, List<ZombieSkill> skills,
                  GameContext ctx, int waveIndex, int difficulty) {
        this.sheet = sheet;
        this.x = startX;
        lastX=(int)startX;
        this.lane = lane;
        this.armors = new ArrayList<>(armors);
        this.skills = new ArrayList<>(skills);
        this.activeEffects = new EnumMap<>(EffectType.class);
        this.context = ctx;
        this.glowing = sheet.isCanSpawnPlantFood() && RNG.nextFloat() < GLOW_CHANCE;

        float diffFactor = difficulty / 3.0f;
        float[] scaled = computeScaledStats(waveIndex);
        this.maxHp         = scaled[0] * diffFactor;
        this.hp            = this.maxHp;
        this.eatDpsPerTick = scaled[1] * diffFactor / TICKS_PER_SECOND;
        this.speedPerTick  = sheet.getSpeed() / TICKS_PER_SECOND;
    }

    // ── TickAware ─────────────────────────────────────────────────────────────

    @Override
    public void enter() {
        currentState = new WalkState();
        currentState.onEnter(this, context);
        context.log("[Spawn] " + sheet.getAlias()
                + " entered lane " + lane + " at x=" + String.format("%.1f", x)
                + (glowing ? " [GLOWING]" : ""));
    }

    @Override
    public void update() {
        if (dead) return;
        tickStatusEffects();
        if (isParalysed()) return;
        ZombieState next = currentState.tick(this, context);
        if (next != currentState) {
            currentState.onExit(this, context);
            next.onEnter(this, context);
            currentState = next;
        }

        //check if Zombie entered new tile
        if (Math.floor(x)!=lastX || lane!=lastLane){
            lastX=(int)Math.floor(x);
            lastLane=lane;
            Tile tile=context.getTileAt(lastX,lane);
            for (TileBehavior b: tile.getBehaviors()){
                b.onZombieEnter(this,tile);
            }
        }
    }

    @Override
    public void dispose() {
        activeEffects.clear();
    }

    // ── Damage API ─────────────────────────────────────────────────────────────

    /**
     * Routes damage through the armour chain then into {@link #hp}.
     *
     * @param amount    positive damage value
     * @param poisonous true → bypasses all armour (Poison Pea, etc.)
     */
    public void takeDamage(float amount, boolean poisonous) {
        if (dead || amount <= 0f) return;
        float remaining = poisonous ? amount : processArmorChain(amount);
        hp = Math.max(0f, hp - remaining);
        if (hp <= 0f && !dead) triggerDeath();
    }

    /** Convenience: non-poisonous damage. */
    public void takeDamage(float amount) {
        takeDamage(amount, false);
    }

    // ── Status-effect API ─────────────────────────────────────────────────────

    /** Applies or refreshes an effect. BURNING clears CHILL and FROZEN. */
    public void applyEffect(StatusEffect effect) {
        if (effect.getType() == EffectType.BURNING) {
            activeEffects.remove(EffectType.CHILL);
            activeEffects.remove(EffectType.FROZEN);
        }
        activeEffects.put(effect.getType(), effect);
    }

    public boolean hasEffect(EffectType type) {
        StatusEffect e = activeEffects.get(type);
        return e != null && e.isActive();
    }

    // ── Speed helper ──────────────────────────────────────────────────────────

    /** Effective speed per tick: halved under CHILL, unchanged otherwise. */
    public float getEffectiveSpeedPerTick() {
        if (hasEffect(EffectType.CHILL)) return speedPerTick * 0.5f;
        return speedPerTick;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ZombiePropertySheet getSheet()       { return sheet; }
    public GameContext getContext()        { return context; }
    public float getX()                         { return x; }
    public void setX(float newX)               { this.x = newX; }
    public int getLane()                        { return lane; }
    public float getHp()                        { return hp; }
    public float getMaxHp()                     { return maxHp; }
    public float getEatDpsPerTick()             { return eatDpsPerTick; }
    public float getSpeedPerTick()              { return speedPerTick; }
    public boolean isDead()                     { return dead; }
    public boolean isGlowing()                  { return glowing; }
    public int getStolenSun()                   { return stolenSun; }
    public void addStolenSun(int amount)        { stolenSun += amount; }
    public boolean isImpAlreadyThrown()         { return impAlreadyThrown; }
    public void markImpThrown()                 { impAlreadyThrown = true; }
    public ZombieState getCurrentState()        { return currentState; }
    public List<ZombieSkill> getSkills()        { return Collections.unmodifiableList(skills); }
    public List<ArmorPiece> getArmors()         { return Collections.unmodifiableList(armors); }
    public Map<EffectType, StatusEffect> getActiveEffects() {
        return Collections.unmodifiableMap(activeEffects);
    }

    // ── CLI display ────────────────────────────────────────────────────────────

    /**
     * Full info string matching the {@code zombies info} output format in the spec.
     * Example:
     * <pre>
     *   ZombieDarkArmor3Default [Intact SHOULDER_ARMOR] [Damaged CROWN]:
     *     position: 6.0, 2
     *     health: 180 / 190
     *     armor:
     *       shoulder_armor: 1600
     *       crown: 800
     *     effects:
     *       chilled: 3.2s
     *     state: Walking
     * </pre>
     */
    public String toInfoString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sheet.getAlias());
        for (ArmorPiece a : armors) {
            String lbl = a.getCurrentArmorStatus();
            if (!lbl.isEmpty()) sb.append(' ').append(lbl);
        }
        sb.append(":\n  position: ").append(String.format("%.1f", x)).append(", ").append(lane);
        sb.append("\n  health: ").append(String.format("%.0f", hp))
          .append(" / ").append(String.format("%.0f", maxHp));
        appendArmorDetails(sb);
        appendEffectDetails(sb);
        sb.append("\n  state: ").append(currentState != null ? currentState.getLabel() : "?");
        return sb.toString();
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private float[] computeScaledStats(int waveIndex) {
        float hpBase  = sheet.getHitPoints();
        float dpsBase = sheet.getEatDps();
        for (ScaledProp prop : sheet.getScaledProps()) {
            switch (prop.getKey()) {
                case "Hitpoints": hpBase  = prop.scale(hpBase,  waveIndex); break;
                case "EatDPS":    dpsBase = prop.scale(dpsBase, waveIndex); break;
                default: break;
            }
        }
        return new float[]{hpBase, dpsBase};
    }

    private float processArmorChain(float amount) {
        for (ArmorPiece armor : armors) {
            if (armor.isDestroyed()) continue;
            float overflow = armor.absorbDamage(amount);
            if (armor.isDestroyed()) {
                context.log(sheet.getAlias() + "'s "
                        + armor.getType().name() + " armour was destroyed!");
            }
            if (!armor.hasFlag(ArmorFlag.PASSDAMAGE)) {
                return overflow;
            }
            // PASSDAMAGE: armour depletes but full amount continues
        }
        return amount;
    }

    private void triggerDeath() {
        dead = true;
        currentState = new DeadState();
        if (glowing) {
            context.addPlantFood(1);
            context.log("The glowing zombie dropped a plant food! [" + sheet.getAlias() + "]");
        }
        if (stolenSun > 0) {
            context.addSun(stolenSun);
            context.log(sheet.getAlias() + " dropped " + stolenSun + " stolen sun on death!");
            stolenSun = 0;
        }
        context.getGameStats().onZombieKilled();
        context.removeZombie(this);
        context.log("Zombie of type " + sheet.getAlias()
                + " is dead at (" + String.format("%.1f", x) + "," + lane + ")");
    }

    private void tickStatusEffects() {
        activeEffects.entrySet().removeIf(entry -> !entry.getValue().tick());
    }

    private boolean isParalysed() {
        return hasEffect(EffectType.FROZEN)
            || hasEffect(EffectType.TRANSFORMED)
            || hasEffect(EffectType.STUN);
    }

    private void appendArmorDetails(StringBuilder sb) {
        sb.append("\n  armor:");
        boolean any = armors.stream().anyMatch(a -> !a.isDestroyed());
        if (!any) { sb.append(" (none)"); return; }
        for (ArmorPiece a : armors) {
            if (!a.isDestroyed()) {
                sb.append("\n    ").append(a.getType().name().toLowerCase())
                  .append(": ").append(String.format("%.0f", a.getCurrentHealth()));
            }
        }
    }

    private void appendEffectDetails(StringBuilder sb) {
        sb.append("\n  effects:");
        if (activeEffects.isEmpty()) { sb.append(" (none)"); return; }
        activeEffects.forEach((type, eff) ->
            sb.append("\n    ").append(type.name().toLowerCase())
              .append(": ").append(String.format("%.1f", eff.getSecondsRemaining())).append("s"));
    }
}
