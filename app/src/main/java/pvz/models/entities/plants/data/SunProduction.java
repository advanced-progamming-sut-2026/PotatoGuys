package pvz.models.entities.plants.data;

/** Parsed shape of a {@code SUN_PRODUCER} plant's autonomous sun output. */
public final class SunProduction {
    private final ProductionKind kind;
    private final float amount;
    private final float[] stages;
    private final float[] stageSeconds;

    public SunProduction(ProductionKind kind, float amount, float[] stages, float[] stageSeconds) {
        this.kind = kind;
        this.amount = amount;
        this.stages = stages == null ? new float[0] : stages.clone();
        this.stageSeconds = stageSeconds == null ? new float[0] : stageSeconds.clone();
    }

    public ProductionKind getKind() { return kind; }
    public float getAmount()        { return amount; }
    public float[] getStages()      { return stages.clone(); }
    public float[] getStageSeconds(){ return stageSeconds.clone(); }

    public float amountAtStage(int stageIndex) {
        if (kind != ProductionKind.STAGED || stages.length == 0) return amount;
        int idx = Math.max(0, Math.min(stageIndex, stages.length - 1));
        return stages[idx];
    }
}
