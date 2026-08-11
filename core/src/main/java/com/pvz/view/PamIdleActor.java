package com.pvz.view;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.pvz.PvZ2;

/**
 * Actor that plays a plant's idle PAM animation, centered and fit inside its cell.
 * <p>
 * Unlike the old version (which polled {@code getClip} until an async load happened to finish
 * and then fell back to a static PNG), this resolves the clip synchronously at construction time
 * via {@link PvZ2#pamPlayer}{@code .clips()}, picks the best available label, and renders with the
 * same {@code draw(path, label, ...)} overload the in-game plant FSM uses, so the animation is
 * guaranteed to play from the first frame. It draws nothing when the PAM is unavailable.
 */
public class PamIdleActor extends Actor {

    private final String pamPath;
    private final String clipLabel;
    private final boolean available;
    private float stateTime;

    private PamIdleActor(String pamPath, String idleLabel) {
        this.pamPath = pamPath;
        List<String> clips;
        try {
            clips = PvZ2.pamPlayer.clips(pamPath); // forces a synchronous bake of the PAM
        } catch (RuntimeException e) {
            clips = null;
        }
        this.clipLabel = pickLabel(clips, idleLabel);
        this.available = clipLabel != null;
    }

    public static PamIdleActor forPlant(PlantData data) {
        if (data == null || data.pamPath() == null) return null;
        return new PamIdleActor(data.pamPath(), data.idleLabel());
    }

    private static String pickLabel(List<String> clips, String preferred) {
        if (clips == null || clips.isEmpty()) return null;
        for (String candidate : new String[]{preferred, "idle", "default", ""}) {
            if (candidate != null && clips.contains(candidate)) return candidate;
        }
        return clips.get(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!available) return;
        Rectangle bounds = PvZ2.pamPlayer.bounds(pamPath, clipLabel);
        if (bounds == null) {
            bounds = PvZ2.pamPlayer.bounds(pamPath);
        }
        float target = Math.min(getWidth(), getHeight());
        float scale;
        if (bounds != null && bounds.width > 0 && bounds.height > 0) {
            scale = Math.min(target / bounds.width, target / bounds.height);
        } else {
            scale = target / 390f; // default PAM canvas is 390x390
        }
        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;
        PvZ2.pamPlayer.draw(batch, pamPath, clipLabel, stateTime, cx, cy, scale, scale, true);
    }
}
