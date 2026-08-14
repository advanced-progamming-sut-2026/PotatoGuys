package com.pvz.models.entities.plants.actions.explosive.potato_mine;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.Explosion;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

import java.util.List;

public class ExplosionAction extends PlantAction {
    String clip;
    float explosionDuration;
    ExplosionType type;
    ExplosionIntensity intensity;
    Zombie target;
    List<Tile> targetTiles;
    float damage;
    public ExplosionAction(String clip, float explosionDuration
        , ExplosionType type, ExplosionIntensity intensity, Zombie target, List<Tile> targetTiles, float damage){
        this.clip=clip;
        this.explosionDuration=explosionDuration;
        this.intensity=intensity;
        this.type=type;
        this.target=target;
        this.targetTiles=targetTiles;
        this.damage=damage;
    }
    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>=explosionDuration){
            ctx.addEffect(new Explosion(ctx,type,intensity,plant.getPosition()));
            if (target!=null) {
                target.takeDamage(damage);
            }
            if (targetTiles!=null && !targetTiles.isEmpty()){
                for (Tile t: targetTiles){
                    for (Zombie z: ctx.getZombiesAt(t.getCol(),t.getLane())){
                        z.takeDamage(damage);
                    }
                    t.processHit(damage);
                }
            }
            plant.dispose();
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath, clip,stateTime,position,scale,null,false);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
