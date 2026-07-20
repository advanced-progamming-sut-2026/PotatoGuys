package pvz.Models.Games.map.behaviors;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.Games.map.Tile;
import pvz.Models.Games.map.TileTags;

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
            tile.getTags().remove(TileTags.GRAVE);
            tile.getTags().remove(TileTags.ICE);
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
