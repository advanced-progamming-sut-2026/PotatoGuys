package pvz.Models.Entities.Plants.data;

/**
 * Parsed shape of a plant's {@code Damage} column (see {@link DamageKind}).
 */
public final class DamageProfile {
    private final DamageKind kind;
    private final float value;
    private final int count;
    private final float[] stages;

    public DamageProfile(DamageKind kind, float value, int count, float[] stages) {
        this.kind = kind;
        this.value = value;
        this.count = count;
        this.stages = stages == null ? new float[0] : stages.clone();
    }

    public DamageKind getKind() { return kind; }
    /** Base damage value (stage-0 / first-pellet value). */
    public float getValue()     { return value; }
    /** Pellet/shot count for {@link DamageKind#MULTI_SHOT}. */
    public int getCount()       { return count; }
    /** Full tier table for {@link DamageKind#STAGED}. */
    public float[] getStages()  { return stages.clone(); }

    /** Damage for a given growth-stage index, clamped to the available stages. */
    public float valueAtStage(int stageIndex) {
        if (kind != DamageKind.STAGED || stages.length == 0) return value;
        int idx = Math.max(0, Math.min(stageIndex, stages.length - 1));
        return stages[idx];
    }
}
