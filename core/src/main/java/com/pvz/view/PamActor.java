package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.pvz.PvZ2;

import pvz.libpvz.pam.ClipRef;

/**
 * Draws a looping PAM animation (idle, by default) scaled to fit this actor's bounds — reusable
 * version of the exact pattern proven in GreenHouseMenu's PlantPamActor, generalized so it isn't
 * plant-specific. Used for zombies' card/details previews, which have PAM animation data
 * (pamFilePath/idleLabel) but no static portrait PNGs at all, unlike plants.
 */
public final class PamActor extends Actor {

    private final String pamPath;
    private final String idleLabel;
    private float stateTime;

    /** @param pamPath the .PAM file path, or null/blank if this zombie has no animation config yet
     *  @param idleLabel the idle clip label to prefer (falls back to "idle"/"default"/"" if not found) */
    public PamActor(String pamPath, String idleLabel) {
        this.pamPath = pamPath;
        this.idleLabel = idleLabel;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (pamPath == null || pamPath.isBlank()) return;
        ClipRef clip = resolveClip();
        if (clip == null) return;

        Rectangle bounds = PvZ2.pamPlayer.bounds(pamPath, idleLabel);
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
        batch.setColor(1f, 1f, 1f, parentAlpha);
        PvZ2.pamPlayer.draw(batch, clip, stateTime, cx, cy, scale, scale, true);
        batch.setColor(Color.WHITE);
    }

    private ClipRef resolveClip() {
        if (idleLabel != null && !idleLabel.isBlank()) {
            ClipRef clip = PvZ2.pamPlayer.getClip(pamPath, idleLabel);
            if (clip != null) return clip;
        }
        for (String state : new String[]{"idle", "default", ""}) {
            ClipRef clip = PvZ2.pamPlayer.getClip(pamPath, state);
            if (clip != null) return clip;
        }
        return null;
    }
}
