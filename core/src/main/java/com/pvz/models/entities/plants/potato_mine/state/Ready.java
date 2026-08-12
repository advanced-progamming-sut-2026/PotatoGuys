package com.pvz.models.entities.plants.potato_mine.state;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.entities.plants.potato_mine.PotatoMine;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

public class Ready extends PlantState {
    public static final String CLIP="idle2";
    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        Zombie target=null;
        float nearestDistance=PotatoMine.ATTACK_RANGE*2;
        for (Zombie z: ctx.getZombiesInLane(plant.getLane())){
            float distance = Math.abs(z.getX()-GameController.colToWorldX(plant.getCol()));
            if (distance<PotatoMine.ATTACK_RANGE && distance<nearestDistance){
                target=z;
                nearestDistance=distance;
            }
        }

        if (target!=null){
            //plant.changeState();
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        return new FrameConfig(PotatoMine.PAM_PATH,CLIP,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
