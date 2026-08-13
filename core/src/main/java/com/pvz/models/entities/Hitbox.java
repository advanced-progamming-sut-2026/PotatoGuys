package com.pvz.models.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * The collision region owned by an {@link Entity}. Wraps a libGDX
 * {@link Rectangle} and keeps a back-reference to the entity that owns it, so
 * overlap queries can resolve "who hit me?" instantly.
 *
 * <p>The rectangle is always centered on its owner's {@code position}; call
 * {@link #centerOn(Vector2)} (or {@link Entity#syncHitbox()}) after the entity
 * moves. Intended to drive overlap-based interactions (projectile hits, zombie
 * eating, AoE damage, ...) via {@link Rectangle#overlaps(Rectangle)}.
 */
public class Hitbox {

    private final Entity owner;
    private final Rectangle rectangle;

    public Hitbox(Entity owner, float width, float height) {
        this.owner = owner;
        this.rectangle = new Rectangle(owner.getPosition().x, owner.getPosition().y, width, height);
        centerOn(owner.getPosition());
    }

    /** Re-centers this hitbox on the given world position (keeps width/height). */
    public void centerOn(Vector2 position) {
        rectangle.setCenter(position.x, position.y);
    }

    /** True if this hitbox intersects {@code other}'s hitbox. */
    public boolean overlaps(Hitbox other) {
        return rectangle.overlaps(other.rectangle);
    }

    public Entity getOwner() {
        return owner;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public float getWidth() {
        return rectangle.width;
    }

    public float getHeight() {
        return rectangle.height;
    }
}
