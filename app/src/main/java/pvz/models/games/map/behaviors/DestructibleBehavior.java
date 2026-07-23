package pvz.models.games.map.behaviors;

import pvz.models.entities.plants.enums.PlantType;
import pvz.models.entities.projectile.Projectile;
import pvz.models.games.GameContext;
import pvz.models.games.card.PlantCard;
import pvz.models.games.map.tile.Tile;
import pvz.models.games.map.tile.TileTags;

public class DestructibleBehavior implements TileBehavior {
    public enum GraveReward {
        NONE, SUN_50, PLANT_FOOD
    }

    private float hp;
    private final String name;
    private final GraveReward reward;

    public DestructibleBehavior(float hp, String name) {
        this(hp, name, GraveReward.NONE);
    }

    public DestructibleBehavior(float hp, String name, GraveReward reward) {
        this.hp = hp;
        this.name = name;
        this.reward = reward;
    }

    public GraveReward getReward() {
        return reward;
    }

    @Override
    public void onProjectileHit(Projectile p, Tile tile) {
        this.hp -= p.getDamage();
        p.destroy(); // Projectile is consumed by grave/ice
        if (this.hp <= 0) {
            // We don't have direct GameContext in onProjectileHit, but projectiles have context or we can add context access if needed.
            // Wait, does Projectile have GameContext? Let's check Projectile.java.
            // Actually, we can grant rewards or log via GameContext if available, or just check tile.
            tile.removeBehavior(this);
            tile.getTags().remove(TileTags.GRAVE);
            tile.getTags().remove(TileTags.ICE_BLOCK);
        }
    }

    public void destroyGrave(Tile tile, GameContext ctx) {
        tile.removeBehavior(this);
        tile.getTags().removeAll(tile.getTags().stream().filter(t->t.equals(TileTags.GRAVE)).toList());
        tile.getTags().remove(TileTags.ICE_BLOCK);

        if (ctx != null) {
            grantReward(ctx);
        }
    }

    public void grantReward(GameContext ctx) {
        if (reward == GraveReward.SUN_50) {
            ctx.addSun(50);
            ctx.log("Grave reward collected: 50 Sun!");
        } else if (reward == GraveReward.PLANT_FOOD) {
            ctx.addPlantFood(1);
            ctx.log("Grave reward collected: Plant Food!");
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
