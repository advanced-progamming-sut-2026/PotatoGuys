package com.pvz.models.entities.plants.data;

public final class GrowthProfile {
    private final float[] stageSeconds;

    public GrowthProfile(float[] stageSeconds) {
        this.stageSeconds = stageSeconds == null ? new float[0] : stageSeconds.clone();
    }

    public float[] getStageSeconds() { return stageSeconds.clone(); }

    public int stageIndexFor(float elapsedSeconds) {
        int stage = 0;
        for (float threshold : stageSeconds) {
            if (elapsedSeconds >= threshold) stage++;
            else break;
        }
        return stage;
    }
}
