package pvz.Models.Entities.Plants.data;

/**
 * Shape of the {@code Damage} column parsed from the plant dataset.
 *
 * <ul>
 *   <li>{@code NONE}      – the plant deals no direct damage (e.g. Sunflower).</li>
 *   <li>{@code FIXED}     – a single flat damage value per hit.</li>
 *   <li>{@code MULTI_SHOT}– {@code count} simultaneous pellets of {@code value} each
 *                           (e.g. Repeater "20x2").</li>
 *   <li>{@code STAGED}    – damage varies by growth stage or bullet variant
 *                           (e.g. Kiwibeast "15/30/45"); {@link #stages} holds
 *                           all tiers, {@link #value} is the base (stage 0).</li>
 *   <li>{@code INSTA_KILL}– bypasses HP entirely (e.g. Chomper).</li>
 * </ul>
 */
public enum DamageKind {
    NONE,
    FIXED,
    MULTI_SHOT,
    STAGED,
    FIRE,
    INSTA_KILL
}
