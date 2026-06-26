package pvz.Models.Projectile;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Engine.TickAware;
import pvz.Models.Seasons.Levels.Level;

public abstract class Projectile implements TickAware {
    Level level;
    protected int damage;
    protected Vector2 position;
    protected Vector2 velocity;
    protected ProjectileType projectileType;
    protected boolean affectedByGravity;

    public Projectile(Level level, int damage , Vector2 currentPosition , Vector2 velocity
            , ProjectileType projectileType, boolean affectedByGravity) {
        this.level=level;
        this.damage = damage;
        this.position = currentPosition;
        this.velocity=velocity;
        this.projectileType = projectileType;
        this.affectedByGravity=affectedByGravity;
    }

    @Override
    public void onFirstTick() {

    }

    @Override
    public void onTick() {
        position.x+=velocity.x;
        position.y=velocity.y;
    }

    @Override
    public void dispose() {
        level.getEngine().getToRemove().add(this);
    }

    public abstract void onCollide(TickAware collidedObj);

    public int getDamage(){
        return damage;
    }
    public Vector2 getCurrentPosition(){
        return position;
    }
    public Vector2 getVelocity(){
        return velocity;
    }
    public ProjectileType getProjectileType() {
        return projectileType;
    }
}
