package com.pvz.models.entities;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.engine.TickAware;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

public class LawnMower implements TickAware {
    private static final String PAM_PATH="768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
    private static final String PAM_LABEL_IDLE="idle";
    private static final String PAM_LABEL_ATTACK="attack";
    private static final float TRIGGER_RADIUS=12f;
    private static final float VELOCITY=300f;

    Vector2 pos;
    GameContext ctx;
    boolean triggered;
    float stateTime;

    public LawnMower(GameContext ctx, int lane){
        pos=new Vector2(GameController.colToWorldX(-1), GameController.laneToWorldY(lane));
        this.ctx=ctx;
        triggered=false;
        stateTime=0;
    }

    @Override
    public void enter() {

    }

    @Override
    public void update(float dt) {
        stateTime+=dt;
        if (!triggered) {
            for (Zombie z : ctx.getZombiesInLane(GameController.worldYtoLane(pos.y))) {
                float distance = z.getX() - pos.x;
                if (distance < TRIGGER_RADIUS) triggered = true;
            }
        } else {
            pos.x+=VELOCITY*dt;
            for (Zombie z : ctx.getZombiesInLane(GameController.worldYtoLane(pos.y))) {
                float distance = z.getX() - pos.x;
                if (distance < TRIGGER_RADIUS) z.takeDamage(100000);
            }
        }
        if (pos.x>GameController.colToWorldX(ctx.getMap().getColumns()+1)){
            dispose();
        }
    }

    @Override
    public FrameConfig draw() {
        if (!triggered) return new FrameConfig(PAM_PATH,PAM_LABEL_IDLE,stateTime,pos,
            new Vector2(0.75f,0.75f),null,true);
        else return new FrameConfig(PAM_PATH,PAM_LABEL_ATTACK,stateTime,pos,
            new Vector2(0.75f,0.75f),null,true);
    }

    @Override
    public void dispose() {
        GameEngine.getInstance().getToRemove().add(this);
        ctx.getLawnMowers()[GameController.worldYtoLane(pos.y)]=null;
    }
}
