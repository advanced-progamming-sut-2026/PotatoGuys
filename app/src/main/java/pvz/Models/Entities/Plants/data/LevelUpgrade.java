package pvz.models.entities.plants.data;

import java.util.List;

/**
 * One entry of the {@code levelUpgrades} array in a plant's JSON profile,
 * corresponding to a single "Lvl 2/3/4" column of the source dataset.
 *
 * <p>Numeric, mechanically-applied deltas live in {@link #getStats()};
 * everything else (flavor toggles like {@code "Double Sun Chance"} or
 * {@code "Can crush 2x"}) is preserved verbatim in {@link #getFlags()} so the
 * data is not lost even though this base architecture does not simulate
 * every unique one-off mechanic.
 */
public final class LevelUpgrade {
    private final int level;
    private final List<StatModifier> stats;
    private final List<String> flags;

    public LevelUpgrade(int level, List<StatModifier> stats, List<String> flags) {
        this.level = level;
        this.stats = List.copyOf(stats);
        this.flags = List.copyOf(flags);
    }

    public int getLevel()               { return level; }
    public List<StatModifier> getStats(){ return stats; }
    public List<String> getFlags()      { return flags; }
}
