package pvz.models.entities.plants.data;

import java.util.List;

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
