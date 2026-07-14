package pvz.Models.Entities.Plants.data;

/** A single numeric stat delta contributed by one {@link LevelUpgrade}. */
public final class StatModifier {
    private final StatKey stat;
    private final float delta;

    public StatModifier(StatKey stat, float delta) {
        this.stat = stat;
        this.delta = delta;
    }

    public StatKey getStat() { return stat; }
    public float getDelta()  { return delta; }

    @Override
    public String toString() { return stat + (delta >= 0 ? "+" : "") + delta; }
}
