package com.pvz.models.entities.plants.actions;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.BonkChoyConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class BonkChoyAttack extends PlantAction{
    public Zombie target;
    private boolean attacked;
    private BonkChoyConfig bonkChoyConfig;

    public BonkChoyAttack(BonkChoyConfig bonkChoyConfig){
        this.bonkChoyConfig=bonkChoyConfig;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        Zombie target=null;
        float minDistance= bonkChoyConfig.attackRangeFactor* GameMap.TILE_WIDTH *2;
        for (Zombie z: ctx.getZombiesInLane(plant.getLane())){
            float distance = z.getX()- GameController.colToWorldX(plant.getCol());
            if (distance>0 && distance< bonkChoyConfig.attackRangeFactor* GameMap.TILE_WIDTH){
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
            this.target=target;
            return true;
        }
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        attacked=false;
        stateTime=0;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (!attacked && stateTime>bonkChoyConfig.intervalSeconds/2){
            if (target != null) target.takeDamage(bonkChoyConfig.baseDamage);
            //self tile
            Tile tile = ctx.getMap().getTileAt(plant.getCol(),plant.getLane());
            tile.processHit(bonkChoyConfig.baseDamage);
            //front tile
            tile = ctx.getMap().getTileAt(plant.getCol()+1,plant.getLane());
            if (tile!=null) tile.processHit(bonkChoyConfig.baseDamage);
            attacked=true;
        }
        if (stateTime>=bonkChoyConfig.intervalSeconds) plant.changeState(new PlantIdleState());
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        target=null;
        attacked=false;
        stateTime=0;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath,pamAnimationConfig.attackActionLabel,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
