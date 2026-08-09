package com.pvz.models.games.map.behaviors;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class GraveBehavior implements TileBehavior {
    public enum GraveReward {
        NONE, SUN_50, PLANT_FOOD
    }

    private float hp;
    private final String name;
    private final GraveReward reward;

    public GraveBehavior(float hp, String name) {
        this(hp, name, GraveReward.NONE);
    }

    public GraveBehavior(float hp, String name, GraveReward reward) {
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
            destroyGrave(tile, AppContext.getInstance().getGameContext());
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
