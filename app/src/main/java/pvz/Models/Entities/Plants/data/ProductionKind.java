package pvz.models.entities.plants.data;

/**
 * How a {@code SUN_PRODUCER} plant generates sun over time.
 *
 * <ul>
 *   <li>{@code FIXED}   – produces the same amount every action interval (Sunflower).</li>
 *   <li>{@code STAGED}  – amount ramps up as the plant grows (Sun-shroom); see
 *                         {@link SunProduction#getStages()} and {@link SunProduction#getStageSeconds()}.</li>
 *   <li>{@code ONESHOT} – produces once then the plant self-destructs (Gold Bloom).</li>
 * </ul>
 */
public enum ProductionKind {
    FIXED,
    STAGED,
    ONESHOT
}
