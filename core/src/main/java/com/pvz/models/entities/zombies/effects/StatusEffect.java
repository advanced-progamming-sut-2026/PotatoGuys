package com.pvz.models.entities.zombies.effects;

/**
 * A live status condition attached to a zombie at runtime.
 *
 * <p>The zombie stores one {@code StatusEffect} per {@link EffectType} in an
 * {@code EnumMap}. Applying the same effect again simply overwrites the
 * previous duration (refresh behaviour).
 *
 * <p>The engine calls {@link #tick()} once per game tick (10 ticks = 1 second).
 * When {@link #tick()} returns {@code false} the effect has expired and should
 * be removed from the map.
 *
 * <h3>Special handling by type</h3>
 * <ul>
 *   <li>{@link EffectType#CHILL}  — halves the zombie's effective speed.</li>
 *   <li>{@link EffectType#FROZEN} — fully stops the zombie; the "ice block" HP
 *       is tracked separately via {@link #iceHp} and reduced by plant attacks.</li>
 *   <li>{@link EffectType#STUN}   — fully stops movement and attacking.</li>
 *   <li>{@link EffectType#POISONED} — triggers {@code poisonDpsPerTick} damage
 *       each tick, bypassing armour.</li>
 * </ul>
 */
public class StatusEffect {

    private static final int DEFAULT_ICE_HP = 600;

    private final EffectType type;
    private float remainingDuration;   // countdown to expiry (infinite = Integer.MAX_VALUE)
    private float poisonDpsPerTick; // used only if type == POISONED
    private float iceHp;           // used only if type == FROZEN

    /**
     * Creates a timed effect with an explicit duration.
     *
     * @param type          the kind of condition
     * @param durationTicks how many ticks until it expires
     */
    public StatusEffect(EffectType type, float duration) {
        this.type = type;
        this.remainingDuration = duration;
        this.iceHp = (type == EffectType.FROZEN) ? DEFAULT_ICE_HP : 0f;
    }

    /** Factory for a POISONED effect with a given DoT rate. */
    public static StatusEffect poisoned(float dpsPerTick, int durationTicks) {
        StatusEffect e = new StatusEffect(EffectType.POISONED, durationTicks);
        e.poisonDpsPerTick = dpsPerTick;
        return e;
    }

    /** Factory for a FROZEN effect using the default ice-block HP (600). */
    public static StatusEffect frozen(float duration) {
        if (duration>0) return new StatusEffect(EffectType.FROZEN, duration);
        return new StatusEffect(EffectType.FROZEN, Integer.MAX_VALUE);
    }

    /**
     * Advance the effect by one tick.
     *
     * @return {@code true} if still active, {@code false} if it just expired
     */
    public boolean update(float dt) {
        if (remainingDuration == Integer.MAX_VALUE) return true; // permanent until removed
        remainingDuration-=dt;
        return remainingDuration > 0;
    }

    /**
     * Absorb fire damage into the ice block.
     * When ice HP hits 0 the caller should remove this effect.
     *
     * @param fireDamage damage dealt to the ice (60/s from nearby fire plants, instant from fireball)
     * @return true if the ice block was broken
     */
    public boolean damageIce(float fireDamage) {
        if (type != EffectType.FROZEN) return false;
        iceHp -= fireDamage;
        return iceHp <= 0;
    }

    public boolean isActive() {
        return remainingDuration > 0;
    }

    public EffectType getType()             { return type; }
    public float getRemainingDuration()          { return remainingDuration; }
    public float getPoisonDpsPerTick()      { return poisonDpsPerTick; }
    public float getIceHp()                 { return iceHp; }
}
