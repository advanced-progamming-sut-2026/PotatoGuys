package pvz.Models.Entities.Plants.data;

/**
 * Optional growth timeline for {@code wramp-up}-tagged plants (Sun-shroom,
 * Kiwibeast): the plant starts weak and ramps up to full strength over the
 * given number of seconds per stage, independent of its action cooldown.
 */
public final class GrowthProfile {
    private final float[] stageSeconds;

    public GrowthProfile(float[] stageSeconds) {
        this.stageSeconds = stageSeconds == null ? new float[0] : stageSeconds.clone();
    }

    public float[] getStageSeconds() { return stageSeconds.clone(); }

    /** Resolves the current 0-based stage index given elapsed seconds since planting. */
    public int stageIndexFor(float elapsedSeconds) {
        int stage = 0;
        for (float threshold : stageSeconds) {
            if (elapsedSeconds >= threshold) stage++;
            else break;
        }
        return stage;
    }
}
