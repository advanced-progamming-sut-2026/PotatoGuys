package pvz.Models.Entities.Plants.data;

/**
 * The finite set of numeric plant stats that a {@link LevelUpgrade} entry can
 * modify. Mirrors the role of {@code ScaledProp}'s key string on the zombie
 * side, but as a closed enum since plant level-ups always target one of these
 * well-known stats (unlike zombie wave-scaling, which is open-ended).
 *
 * <p>Any upgrade token from the CSV/JSON that does not map to one of these
 * keys (e.g. {@code "Double Sun Chance"}, {@code "Zombie HP Buff"}) is kept
 * as a free-form flag string instead — see {@link LevelUpgrade#getFlags()}.
 */
public enum StatKey {
    MAX_HP,
    DAMAGE,
    SUN_COST,
    ACTION_INTERVAL_SECONDS,
    RECHARGE_SECONDS,
    RANGE_TILES,
    PIERCE_COUNT,
    ATK_SPEED_PERCENT,
    EFFECT_DURATION_SECONDS
}
