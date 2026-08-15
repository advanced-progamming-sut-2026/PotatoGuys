
package com.pvz.models.entities.effects;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.games.GameContext;

/**
 * Visual sandstorm cloud used to carry a zombie in from off-map (Ancient
 * Egypt final-wave bursts). Owns purely the *visual* clip sequencing; the
 * movement/arrival decision belongs to
 * {@link com.pvz.models.entities.zombies.fsm.SandstormCarryState}, which
 * creates one of these per carried zombie and drives it via
 * {@link #isReadyToMove()} / {@link #beginOutro()}.
 *
 * <p>Plays three clips back-to-back, exactly like the in-game sandstorm:
 * <ol>
 *   <li><b>INTRO</b> — the storm forms at the zombie's spawn point. The
 *       zombie does not move yet.</li>
 *   <li><b>LOOP</b>  — the storm travels with the zombie, looping
 *       indefinitely until {@link #beginOutro()} is called.</li>
 *   <li><b>OUTRO</b> — the storm dissipates in place (it stops following the
 *       rider once this starts). The effect disposes itself once this clip
 *       finishes, exactly like {@code Explosion}/{@code JalapenoFire} do.</li>
 * </ol>
 */
public class SandstormCloudEffect extends Effect {

    // TODO: replace with the real PAM path once the asset address is known.
    private static final String SAND_STORM_PAM_PATH = "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";

    // TODO: replace with the real clip names baked into the PAM above.
    private static final String INTRO_CLIP = "intro";
    private static final String LOOP_CLIP = "loop";
    private static final String OUTRO_CLIP = "outro";

    private static final Vector2 SCALE = new Vector2(0.65f, 0.65f);

    // Used only if the clip isn't found in the AnimationCatalog yet (e.g. the
    // placeholder PAM path above hasn't been wired to real data).
    private static final float FALLBACK_INTRO_DURATION = 0.5f;
    private static final float FALLBACK_OUTRO_DURATION = 0.5f;

    private enum Phase { INTRO, LOOP, OUTRO }

    /** The entity this cloud stays glued to while traveling (the zombie). */
    private final Entity rider;

    private final float introDuration;
    private final float outroDuration;

    private Phase phase = Phase.INTRO;
    private float clipTime;

    public SandstormCloudEffect(GameContext ctx, Entity rider) {
        super(ctx, rider.getPosition());
        this.rider = rider;

        AnimationCatalog catalog = AnimationCatalog.getInstance();
        this.introDuration = resolveDuration(catalog, INTRO_CLIP, FALLBACK_INTRO_DURATION);
        this.outroDuration = resolveDuration(catalog, OUTRO_CLIP, FALLBACK_OUTRO_DURATION);
    }

    private float resolveDuration(AnimationCatalog catalog, String clip, float fallback) {
        float duration = catalog != null ? catalog.getClipDuration(SAND_STORM_PAM_PATH, clip) : -1f;
        return duration > 0f ? duration : fallback;
    }

    /** True once the intro has finished forming, i.e. it's safe to start moving the rider. */
    public boolean isReadyToMove() {
        return phase != Phase.INTRO;
    }

    /**
     * Freezes the cloud in place and starts the dissipate animation.
     * Idempotent — safe to call more than once.
     */
    public void beginOutro() {
        if (phase == Phase.OUTRO) return;
        phase = Phase.OUTRO;
        clipTime = 0f;
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        // Stay glued to the rider while forming/traveling; once dissipating,
        // stay put at the drop point instead of trailing the (now-visible,
        // already-walking) zombie.
        if (phase != Phase.OUTRO) {
            pos.set(rider.getPosition());
        }

        clipTime += dt;
        if (phase == Phase.INTRO && clipTime >= introDuration) {
            phase = Phase.LOOP;
            clipTime = 0f;
        } else if (phase == Phase.OUTRO && clipTime >= outroDuration) {
            dispose();
        }
    }

    @Override
    public FrameConfig draw() {
        String clip = switch (phase) {
            case INTRO -> INTRO_CLIP;
            case LOOP -> LOOP_CLIP;
            case OUTRO -> OUTRO_CLIP;
        };
        // Only the LOOP clip should actually loop; intro/outro play once.
        boolean looping = phase == Phase.LOOP;
        return new FrameConfig(SAND_STORM_PAM_PATH, clip, clipTime, pos, SCALE, null, looping);
    }
}
