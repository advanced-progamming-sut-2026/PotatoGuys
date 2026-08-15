package com.pvz.models.entities.plants.actions.explosive.squash;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.SquashConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

public class SquashAction extends PlantAction {
    SquashConfig config;
    Zombie target;

    public SquashAction(SquashConfig config){
        this.config=config;
        this.target=null;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        float nearestDistance= config.radarRangeCoefficient* GameMap.TILE_WIDTH *2;
        for (Zombie z: ctx.getZombiesInLane(plant.getLane())){
            float distance = Math.abs(z.getX()- GameController.colToWorldX(plant.getCol()));
            if (distance<config.radarRangeCoefficient*GameMap.TILE_WIDTH && distance<nearestDistance){
                target=z;
                nearestDistance=distance;
            }
        }
        return target!=null;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (target.getX()-plant.getPosition().x>0){
            plant.changeState(new SquashJumpAction(config,target,true));
        } else {
            plant.changeState(new SquashTurnAction(config,target));
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath,pamAnimationConfig.idleLabel,stateTime,plant.getPosition(),scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
