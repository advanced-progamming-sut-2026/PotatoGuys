package com.pvz.models.entities;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.TickAware;

/**
 * Base class for every moving/collidable object in the world (plants, zombies,
 * projectiles, suns, lawn mowers, ...).
 *
 * <p>Holds the common physical state shared by all entities:
 * <ul>
 *   <li>{@link #position} — the entity's center in world coordinates;</li>
 *   <li>{@link #velocity} — movement in world units per second;</li>
 *   <li>{@link #hitbox}   — an {@link Hitbox} (a {@code Rectangle} that remembers
 *       its owning entity) used for overlap-based interactions.</li>
 * </ul>
 *
 * <p>Subclasses call {@link #setHitbox(float, float)} once and {@link #syncHitbox()}
 * after they move, then use {@link #overlaps(Entity)} to drive collision logic.
 */
public abstract class Entity implements TickAware {

    protected final Vector2 position = new Vector2();
    protected final Vector2 velocity = new Vector2();

    protected Hitbox hitbox;

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        syncHitbox();
    }

    public void setPosition(Vector2 newPosition) {
        position.set(newPosition);
        syncHitbox();
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }

    public void setVelocity(Vector2 newVelocity) {
        velocity.set(newVelocity);
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    /**
     * Creates this entity's hitbox, sized {@code width} x {@code height} and
     * centered on the current position. Call once after construction.
     */
    public void setHitbox(float width, float height) {
        hitbox = new Hitbox(this, position.x, position.y, width, height);
    }

    /**
     * Installs an already-built hitbox (typically an anonymous
     * {@code new Hitbox(...) { @Override onCollision(...) } } subclass) and
     * centers it on the current position.
     */
    public void setHitbox(Hitbox hitbox) {
        this.hitbox = hitbox;
        syncHitbox();
    }

    /** Re-centers the hitbox on the current position; call after moving. */
    protected void syncHitbox() {
        if (hitbox != null) {
            hitbox.centerOn(position);
        }
    }

    /**
     * True if this entity's hitbox overlaps {@code other}'s. Both entities must
     * have a hitbox for this to be meaningful.
     */
    public boolean overlaps(Entity other) {
        return hitbox != null && other != null && other.hitbox != null && hitbox.overlaps(other.hitbox);
    }
}
