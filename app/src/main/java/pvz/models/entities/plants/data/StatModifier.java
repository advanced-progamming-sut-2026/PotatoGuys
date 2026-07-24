package pvz.models.entities.plants.data;

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
