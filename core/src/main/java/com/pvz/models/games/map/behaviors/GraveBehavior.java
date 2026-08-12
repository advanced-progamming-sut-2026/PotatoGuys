package com.pvz.models.games.map.behaviors;

import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class GraveBehavior implements TileBehavior {
    private static final String pamId="768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
    private static final String clip="undamaged";

    float stateTime;

    public enum GraveReward {
        NONE, SUN_50, PLANT_FOOD
    }

    private float hp;
    private final String name;
    private final GraveReward reward;
    private final Tile tile;

    public GraveBehavior(Tile tile, float hp, String name) {
        this(tile, hp, name, GraveReward.NONE);
    }

    public GraveBehavior(Tile tile, float hp, String name, GraveReward reward) {
        this.tile=tile;
        this.hp = hp;
        this.name = name;
        this.reward = reward;
        stateTime=0;
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
    public void processHit(float damage) {
        TileBehavior.super.processHit(damage);
        hp-=damage;
        if (this.hp <= 0) {
            destroyGrave(tile, AppContext.getInstance().getGameContext());
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

    @Override
    public void update(GameContext ctx, Tile tile, float dt) {
        TileBehavior.super.update(ctx, tile, dt);
        stateTime+=dt;
    }

    @Override
    public void draw(Tile tile) {
        TileBehavior.super.draw(tile);
        PvZ2.pamPlayer.draw(PvZ2.batch,pamId,clip,stateTime,tile.getX()+Tile.WIDTH/2,tile.getY()+Tile.HEIGHT/2,0.6f,0.6f,false);
    }
}
