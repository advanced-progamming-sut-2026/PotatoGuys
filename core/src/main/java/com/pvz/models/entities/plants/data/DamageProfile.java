package com.pvz.models.entities.plants.data;

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
    public float getValue()     { return value; }
    public int getCount()       { return count; }
    public float[] getStages()  { return stages.clone(); }

    public float valueAtStage(int stageIndex) {
        if (kind != DamageKind.STAGED || stages.length == 0) return value;
        int idx = Math.max(0, Math.min(stageIndex, stages.length - 1));
        return stages[idx];
    }
}
