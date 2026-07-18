package pvz.Models.Games.map.behaviors;

import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Games.map.Tile;

public class DestructibleBehavior implements TileBehavior {
    private float hp;
    private final String name;

    public DestructibleBehavior(float hp, String name) {
        this.hp = hp;
        this.name = name;
    }

    @Override
    public void onProjectileHit(Projectile p, Tile tile) {
        this.hp -= p.getDamage();
        p.spend(); // Projectile is consumed by grave/ice
        if (this.hp <= 0) {
            tile.removeBehavior(this);
        }
    }

    @Override
    public String getName() { return name; }
}
