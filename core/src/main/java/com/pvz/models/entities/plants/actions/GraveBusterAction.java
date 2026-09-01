package com.pvz.models.entities.plants.actions;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.GraveBusterConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.grave_buster.GraveBuster;
import com.pvz.models.entities.plants.grave_buster.state.Eat;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class GraveBusterAction extends PlantAction{
    GraveBusterConfig config;
    float stateTime2;
    public GraveBusterAction(GraveBusterConfig config){
        this.config=config;
        stateTime2=0;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime2+=dt;
        return stateTime2 >= config.initDuration;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime=0;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>= config.eatDuration){
            Tile tile = ctx.getMap().getTileAt(plant.getCol(),plant.getLane());
            tile.getBehaviors().removeIf(behavior -> behavior instanceof GraveBehavior);
            tile.getTags().remove(TileTags.GRAVE);
            plant.dispose();
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        stateTime=0;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath,config.label,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public boolean isPlantableOnTile(Tile tile) {
        return tile.getTags().contains(TileTags.GRAVE);
    }
}
