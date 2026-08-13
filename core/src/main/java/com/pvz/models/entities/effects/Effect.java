package com.pvz.models.entities.effects;

import com.pvz.models.engine.TickAware;

public abstract class Effect implements TickAware {
    protected float stateTime;
    public Effect(){
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

    }
}
