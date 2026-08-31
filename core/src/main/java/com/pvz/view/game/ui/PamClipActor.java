package com.pvz.view.game.ui;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.pvz.PvZ2;

/**
 * Plays a single named clip of a PAM animation, always looping. Used in two
 * roles:
 *
 * <ul>
 * <li><b>Sticker preview:</b> with a {@code scale &lt;= 0} the clip is scaled to
 * fit the actor's cell and loops forever (no auto-removal).</li>
 * <li><b>Opponent sticker:</b> with a fixed positive {@code scale} and a
 * {@code seconds} duration the clip is rendered as a loop for {@code seconds}
 * and the actor then removes itself.</li>
 * </ul>
 *
 * <p>
 * The clip is resolved synchronously at construction (forcing a one-time bake,
 * same as {@link com.pvz.view.PamIdleActor}) so the animation is guaranteed to
 * start on the first frame. Nothing is drawn if the PAM/clip is unavailable.
 */
public class PamClipActor extends Actor {

    private final String pamPath;
    private final String clipLabel;
    private final boolean available;
    private final float scale;
    private final float seconds;
    private final boolean timed;
    private final boolean staticFrame;
    private float stateTime;

    /**
     * @param pamPath PAM path relative to the assets root.
     * @param clip desired clip label (validated at construction).
     * @param scale draw scale; {@code <= 0} means "fit to this actor's cell".
     * @param seconds duration before self-removal; {@code <= 0} loops forever.
     */
    public PamClipActor(String pamPath, String clip, float scale, float seconds) {
        this(pamPath, clip, scale, seconds, false);
    }

    /**
     * Creates a clip actor, optionally pinned to a single static frame.
     *
     * @param pamPath PAM path relative to the assets root.
     * @param clip desired clip label (validated at construction).
     * @param scale draw scale; {@code <= 0} means "fit to this actor's cell".
     * @param seconds duration before self-removal; {@code <= 0} loops forever.
     * @param staticFrame when true the animation is never advanced, drawing one
     *            fixed frame (the first) instead of looping.
     */
    public PamClipActor(String pamPath, String clip, float scale, float seconds, boolean staticFrame) {
        super();
        this.pamPath = pamPath;
        this.scale = scale;
        this.seconds = seconds;
        this.timed = seconds > 0f;
        this.staticFrame = staticFrame;

        String resolved = null;
        if (pamPath != null) {
            List<String> clips;
            try {
                clips = PvZ2.pamPlayer.clips(pamPath);
            } catch (RuntimeException e) {
                clips = null;
            }
            resolved = pickLabel(clips, clip);
        }
        this.clipLabel = resolved;
        this.available = resolved != null;
    }

    private static String pickLabel(List<String> clips, String preferred) {
        if (clips == null || clips.isEmpty())
            return null;
        if (preferred != null && clips.contains(preferred))
            return preferred;
        return clips.get(0);
    }

    /** True when the PAM/clip resolved and the actor can play. */
    public boolean isAvailable() {
        return available;
    }

    /** The clip label actually resolved (may fall back to another label). */
    public String resolvedClip() {
        return clipLabel;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (available && !staticFrame) {
            stateTime += delta;
            if (timed && stateTime >= seconds) {
                remove();
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!available)
            return;
        float drawScale = scale;
        if (drawScale <= 0f) {
            Rectangle bounds = PvZ2.pamPlayer.bounds(pamPath, clipLabel);
            if (bounds == null) {
                bounds = PvZ2.pamPlayer.bounds(pamPath);
            }
            float target = Math.min(getWidth(), getHeight());
            if (bounds != null && bounds.width > 0 && bounds.height > 0) {
                drawScale = Math.min(target / bounds.width, target / bounds.height);
            } else {
                drawScale = target / 390f;
            }
        }
        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;
        float playTime = staticFrame ? 0f : stateTime;
        PvZ2.pamPlayer.draw(batch, pamPath, clipLabel, playTime, cx, cy, drawScale, drawScale, true);
    }
}