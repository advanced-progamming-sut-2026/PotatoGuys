package com.pvz.models.entities.plants.bonk_choy.state;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.bonk_choy.BonkChoy;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class Idle extends PlantState {
    private static final String CLIP="idle";
    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        Zombie target=null;
        float minDistance= BonkChoy.DEFAULT_ATTACK_RANGE*2;
        for (Zombie z: ctx.getZombiesInLane(plant.getLane())){
            float distance = z.getX()-GameController.colToWorldX(plant.getCol());
            if (distance>0 && distance< BonkChoy.DEFAULT_ATTACK_RANGE){
                if (distance<minDistance){
                    target=z;
                    minDistance=distance;
                }
            }
        }

        Tile tile = ctx.getMap().getTileAt(plant.getCol()+1,plant.getLane());
        boolean frontTileHasDestructible=false;
        if (tile!=null && (tile.getTags().contains(TileTags.GRAVE) || tile.getTags().contains(TileTags.ICE_BLOCK))){
            frontTileHasDestructible = true;
        }
        if (target!=null || frontTileHasDestructible){
            plant.changeState(new Attack(target));
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        return new FrameConfig(BonkChoy.PAM_PATH,CLIP,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
