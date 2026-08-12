package com.pvz.models.entities.plants.grave_buster.state;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.bonk_choy.BonkChoy;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.entities.plants.grave_buster.GraveBuster;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class Eat extends PlantState {
    public static final String CLIP="attack1";
    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>= GraveBuster.DEFAULT_EAT_DURATION){
            Tile tile = ctx.getMap().getTileAt(plant.getCol(),plant.getLane());
            tile.getBehaviors().removeIf(behavior -> behavior instanceof GraveBehavior);
            tile.getTags().remove(TileTags.GRAVE);
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
        return new FrameConfig(GraveBuster.PAM_PATH,CLIP,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
