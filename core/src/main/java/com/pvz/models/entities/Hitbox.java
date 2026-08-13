package com.pvz.models.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * The collision region owned by an {@link Entity}. Wraps a libGDX
 * {@link Rectangle} and keeps a back-reference to the entity that owns it, so
 * overlap queries can resolve "who hit me?" instantly.
 *
 * <p>The rectangle is always centered on its owner's {@code position}; call
 * {@link #centerOn(Vector2)} after the entity moves. Every tick the
 * {@link com.pvz.models.engine.CollisionSystem} sweeps every registered hitbox
 * and, for each overlapping pair, calls {@link #onCollision(Hitbox)} on
 * <b>both</b> sides — it never cares what entity owns the hitbox.
 *
 * <p>The collision reaction is overridable at construction time by subclassing:
 * <pre>{@code
 * setHitbox(new Hitbox(this, 32f, 32f) {
 *     @Override
 *     public void onCollision(Hitbox onHit) {
 *         if (onHit.getOwner() instanceof Zombie zombie) {
 *             zombie.takeDamage(50);
 *         }
 *     }
 * });
 * }</pre>
 * This is the only collision hook — there is no entity-level {@code onCollision}.
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

    /**
     * Called by the {@link com.pvz.models.engine.CollisionSystem} whenever this
     * hitbox overlaps {@code other}. Override it to react; the default does
     * nothing.
     *
     * @param onHit the hitbox this one collided with (never null)
     */
    public void onCollision(Hitbox onHit) {
        // default: no reaction
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
