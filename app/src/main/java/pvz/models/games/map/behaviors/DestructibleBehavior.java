package pvz.models.games.map.behaviors;

import pvz.models.entities.plants.enums.PlantType;
import pvz.models.entities.projectile.Projectile;
import pvz.models.games.card.PlantCard;
import pvz.models.games.map.tile.Tile;
import pvz.models.games.map.tile.TileTags;

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
        p.destroy(); // Projectile is consumed by grave/ice
        if (this.hp <= 0) {
            tile.removeBehavior(this);
            tile.getTags().remove(TileTags.GRAVE);
            tile.getTags().remove(TileTags.ICE_BLOCK);
        }
    }

    @Override
    public String getStatus() {
        return "\n    HP: "+hp;
    }

    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
        return p.getPlant().getType() == PlantType.GraveBuster;
    }

    @Override
    public String getName() { return name; }
}
