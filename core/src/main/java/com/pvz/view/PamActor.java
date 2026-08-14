package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.pvz.PvZ2;
import pvz.libpvz.pam.ClipRef;

import java.util.List;
import java.util.Map;

/**
 * Draws a looping PAM animation (idle, by default) scaled to fit this actor's bounds.
 *
 * <p>Unlike the old version (which polled {@code getClip} until an async load happened to
 * finish and so usually drew nothing), this resolves the clip synchronously at construction
 * time via {@link PvZ2#pamPlayer}{@code .clips()}, exactly like {@link PamIdleActor} does
 * for plants, so the animation is guaranteed to play from the first frame.
 *
 * <p>Armored zombies share the same sheet as their basic zombie: the PAM contains every
 * armor variant (cone/bucket/brick/crown/shoulder...) as parts that are hidden by default.
 * The optional {@code partVisibility} map force-shows the named parts (e.g.
 * {@code "zombie_armor_cone_norm"}) so the preview displays the zombie's own armor.
 */
public final class PamActor extends Actor {

    private final String pamPath;
    private final String clipLabel;
    private final Map<String, Boolean> partVisibility;
    private final boolean available;
    private float stateTime;

    /** @param pamPath the .PAM file path, or null/blank if this zombie has no animation config yet
     *  @param idleLabel the idle clip label to prefer (falls back to "idle"/"default"/"" if not found) */
    public PamActor(String pamPath, String idleLabel) {
        this(pamPath, idleLabel, null);
    }

    /** @param partVisibility part names to force visible (e.g. armor), or null for the default view */
    public PamActor(String pamPath, String idleLabel, Map<String, Boolean> partVisibility) {
        this.pamPath = pamPath;
        this.partVisibility = partVisibility;
        List<String> clips;
        try {
            clips = PvZ2.pamPlayer.clips(pamPath); // forces a synchronous bake of the PAM
        } catch (RuntimeException e) {
            clips = null;
        }
        this.clipLabel = pickLabel(clips, idleLabel);
        this.available = clipLabel != null;
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
        batch.setColor(1f, 1f, 1f, parentAlpha);
        if (partVisibility != null) {
            drawWithVisibility(batch, pamPath, clipLabel, stateTime, cx, cy, scale,
                    true, partVisibility);
        } else {
            PvZ2.pamPlayer.draw(batch, pamPath, clipLabel, stateTime, cx, cy, scale, scale, true);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * Draws the animation with both a scale and a part-visibility map. The public
     * {@code PamPlayer} visibility overloads only draw at unit scale, so this mirrors
     * them by invoking the same {@code drawInternal} the public API uses.
     */
    public static void drawWithVisibility(Batch batch, String pamPath, String clipLabel,
                                          float stateTime, float cx, float cy, float scale,
                                          boolean loop, Map<String, Boolean> visibility) {
        try {
            ClipRef clip = PvZ2.pamPlayer.getClip(pamPath, clipLabel);
            if (clip == null) return;
            Class<?> clipClass = ClipRef.class;
            java.lang.reflect.Field baField = clipClass.getDeclaredField("ba");
            java.lang.reflect.Field rangeField = clipClass.getDeclaredField("range");
            baField.setAccessible(true);
            rangeField.setAccessible(true);
            Object ba = baField.get(clip);
            int[] range = (int[]) rangeField.get(clip);
            java.lang.reflect.Method m = PvZ2.pamPlayer.getClass().getDeclaredMethod(
                    "drawInternal", Batch.class, ba.getClass(), int[].class,
                    float.class, boolean.class, float.class, float.class,
                    float.class, float.class, Map.class, String.class);
            m.setAccessible(true);
            m.invoke(PvZ2.pamPlayer, batch, ba, range, stateTime, loop,
                    cx, cy, scale, scale, visibility, null);
        } catch (ReflectiveOperationException e) {
            PvZ2.pamPlayer.draw(batch, pamPath, clipLabel, stateTime, cx, cy, scale, scale, true);
        }
    }
}
