package com.pvz.models.entities.plants.actions.explosive.potato_mine;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.ExplosiveConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class ExplosiveAction extends PlantAction {
    ExplosiveConfig config;
    float stateTime2;
    private boolean triggred;

    public ExplosiveAction(ExplosiveConfig config){
        this.config=config;
        stateTime2=0;
        triggred=false;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime2+=dt;
        if (stateTime2>=config.plantTime && !triggred){
            triggred=true;
            return true;
        }
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>=config.idleTime) {
            if (plant.getType()== PlantType.CherryBomb || plant.getType()==PlantType.Jalapeno){
                List<Tile> targetTiles=new ArrayList<>();
                if (plant.getType()== PlantType.CherryBomb){
                    for (int i = plant.getCol()-1; i < plant.getCol()+2; i++) {
                        for (int j = plant.getLane()-1; j < plant.getLane()+2; j++) {
                            Tile tile=ctx.getMap().getTileAt(i,j);
                            if (tile!=null) targetTiles.add(tile);
                        }
                    }
                } else{
                    for (int i = 0; i < ctx.getMap().getColumns(); i++) {
                        Tile tile = ctx.getMap().getTileAt(i, plant.getLane());
                        if (tile != null) targetTiles.add(tile);
                    }
                }
                plant.changeState(new ExplosionAction(config.explosionClip,
                    config.explosionTime,config.explosionType,config.explosionIntensity,
                    null,targetTiles,config.baseDamage));
            } else {
                plant.changeState(new PotatoMineReadyAction(config));
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
        return new FrameConfig(pamAnimationConfig.pamFilePath,config.idleClip,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
