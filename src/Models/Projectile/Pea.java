package Models.Projectile;

import Models.DataTypes.Vector2;
import Models.Engine.TickAware;
import Models.Seasons.Levels.Level;
import Models.Zombies.Zombie;

public class Pea extends Projectile {
    public Pea(Level level, int damage, Vector2 currentPosition, Vector2 velocity, ProjectileType projectileType, boolean affectedByGravity) {
        super(level, damage, currentPosition, velocity, projectileType, affectedByGravity);
    }

    @Override
    public void onCollide(TickAware collidedObj) {
        if (collidedObj instanceof Zombie zombie){
            zombie.takeDamage(damage);
        }
        dispose();
    }
}
