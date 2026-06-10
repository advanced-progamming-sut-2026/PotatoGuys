package Models.Projectile;

import javax.swing.text.Position;

public abstract class Projectile {
    private int damage;
    private Position currentPosition;
    private int movementSpeed;
    private ProjectileType projectileType;

    public Projectile(int damage , Position currentPosition , int movementSpeed , ProjectileType projectileType) {
        this.damage = damage;
        this.currentPosition = currentPosition;
        this.movementSpeed = movementSpeed;
        this.projectileType = projectileType;
    }
    public int getDamage(){
        return damage;
    }
    public Position getCurrentPosition(){
        return currentPosition;
    }
    public int getMovementSpeed(){
        return movementSpeed;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }
}
