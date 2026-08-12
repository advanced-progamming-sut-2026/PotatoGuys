package com.pvz.models.entities.plants.bonk_choy.state;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.TickAware;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.bonk_choy.BonkChoy;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

import java.util.ArrayList;
import java.util.List;

public class Attack extends PlantState {
    private static final String CLIP="attack";

    private Zombie target;
    private boolean attacked;

    public Attack(Zombie target){
        this.target=target;
        attacked=false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (!attacked && stateTime>BonkChoy.DEFAULT_ACTION_INTERVAL/2){
            if (target != null) target.takeDamage(BonkChoy.BASE_DAMAGE);
            Tile tile = ctx.getMap().getTileAt(plant.getCol(),plant.getLane());
            tile.processHit(BonkChoy.BASE_DAMAGE);
            attacked=true;
        }
        if (stateTime>=BonkChoy.DEFAULT_ACTION_INTERVAL) plant.changeState(new Idle());
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.7f,0.7f);
        return new FrameConfig(BonkChoy.PAM_PATH,CLIP,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
