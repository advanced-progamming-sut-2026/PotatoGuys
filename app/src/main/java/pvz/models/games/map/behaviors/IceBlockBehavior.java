package pvz.models.games.map.behaviors;

import pvz.models.entities.plants.Plant;
import pvz.models.games.map.tile.Tile;
import pvz.models.entities.projectile.Projectile;

public class IceBlockBehavior implements TileBehavior {
    private float iceHp = 600f;
    private Object entityInside; // Can be Plant or Zombie

    public IceBlockBehavior(Object entity) {
        this.entityInside = entity;
        if (entity instanceof Plant p) {
            p.incrementFreezeLevel(); // Set it to frozen state
        }
        // Zombies are already frozen via StatusEffect or this behavior
    }

    @Override
    public void onProjectileHit(Projectile p, Tile tile) {
        boolean isFire = p.isFire();
        float damage = p.getDamage();
        
        if (isFire) {
            iceHp = 0f;
        } else {
            iceHp = Math.max(0f, iceHp - damage);
        }
        
        if (iceHp <= 0f) {
            tile.removeBehavior(this);
            // Release entity
            if (entityInside instanceof Plant plant) {
                plant.takeIceDamage(0f, true); // Melts it
            }
            // For Zombie, the paralysis effect will expire naturally
        }
    }

    public void takeDamage(float amount, boolean isFire) {
        if (isFire) {
            iceHp = 0f;
        } else {
            iceHp = Math.max(0f, iceHp - amount);
        }
    }
    
    public float getIceHp() { return iceHp; }
    public Object getEntityInside() { return entityInside; }

    @Override
    public String getName() { return "IceBlock"; }
}
