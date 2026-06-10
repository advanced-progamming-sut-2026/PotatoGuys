package Models.Projectile;

import Models.DataTypes.Vector2;

public abstract class Projectile {
    private int damage;
    private Vector2 currentPosition;
    private Vector2 velocity;
    private ProjectileType projectileType;

    public Projectile(int damage , Vector2 currentPosition , Vector2 velocity , ProjectileType projectileType) {
        this.damage = damage;
        this.currentPosition = currentPosition;
        this.velocity=velocity;
        this.projectileType = projectileType;
    }
    public int getDamage(){
        return damage;
    }
    public Vector2 getCurrentPosition(){
        return currentPosition;
    }
    public Vector2 getVelocity(){
        return velocity;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }
}
