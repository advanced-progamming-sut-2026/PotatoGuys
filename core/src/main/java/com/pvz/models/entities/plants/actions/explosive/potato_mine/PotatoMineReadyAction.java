package com.pvz.models.entities.plants.actions.explosive.potato_mine;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.actions.explosive.ExplosionAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.ExplosiveConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.plants.potato_mine.PotatoMine;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class PotatoMineReadyAction extends PlantAction {
    ExplosiveConfig config;
    String currentClip;

    public PotatoMineReadyAction(ExplosiveConfig config){
        this.config=config;
        currentClip=config.recoverClip;
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
        if (stateTime>config.recoverTime){
            if (!currentClip.equals(config.readyClip)) {
                currentClip = config.readyClip;
            }
            Zombie target=null;
            float nearestDistance= PotatoMine.ATTACK_RANGE*2;
            for (Zombie z: ctx.getZombiesInLane(plant.getLane())){
                float distance = Math.abs(z.getX()- GameController.colToWorldX(plant.getCol()));
                if (distance<PotatoMine.ATTACK_RANGE && distance<nearestDistance){
                    target=z;
                    nearestDistance=distance;
                }
            }
            if (target!=null){
                List<Tile> targetTiles=new ArrayList<>();
                if (plant.getType()== PlantType.PrimalPotatoMine){
                    for (int i = plant.getCol()-1; i < plant.getCol()+2; i++) {
                        for (int j = plant.getLane()-1; j < plant.getLane()+2; j++) {
                            Tile tile=ctx.getMap().getTileAt(i,j);
                            if (tile!=null) targetTiles.add(tile);
                        }
                    }
                }
                plant.changeState(new ExplosionAction(config, target, targetTiles));
            }
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
        return new FrameConfig(pamAnimationConfig.pamFilePath,currentClip,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
