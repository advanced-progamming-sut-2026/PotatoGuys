package com.pvz.controller.game;

/**
 * A purely decorative zombie shown during the intro camera pan, before the real
 * game session (and its GameContext) exists.
 *
 * <p>It is not a full {@link com.pvz.models.entities.zombies.Zombie} entity: it
 * carries only the data the renderer needs to loop a single idle PAM clip at a
 * fixed world position.
 */
public class DisplayZombie {

    public final String pamPath;
    public final String idleClip;
    public final float x;
    public final float y;
    public final float scale;

    public DisplayZombie(String pamPath, String idleClip, float x, float y, float scale) {
        this.pamPath = pamPath;
        this.idleClip = idleClip;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
}
