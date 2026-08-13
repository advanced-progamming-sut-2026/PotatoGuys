package com.pvz.models.entities.effects;

import com.pvz.models.engine.GameEngine;
import com.pvz.models.engine.TickAware;
import com.pvz.models.games.GameContext;

public abstract class Effect implements TickAware {
    protected float stateTime;
    protected GameContext ctx;
    public Effect(GameContext ctx){
        this.ctx=ctx;
        stateTime=0;
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(float dt) {
        stateTime+=dt;
    }

    @Override
    public void dispose() {
        GameEngine.getInstance().getToRemove().add(this);
        ctx.removeEffect(this);
    }
}
