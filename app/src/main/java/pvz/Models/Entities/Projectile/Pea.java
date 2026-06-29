package pvz.Models.Entities.Projectile;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Seasons.Levels.Level;

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
