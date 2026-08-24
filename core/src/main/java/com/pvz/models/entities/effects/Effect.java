package com.pvz.models.entities.effects;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.TickAware;
import com.pvz.models.games.GameContext;

public abstract class Effect implements TickAware {
    protected float stateTime;
    protected Vector2 pos;
    protected GameContext ctx;

    public Effect(GameContext ctx, Vector2 pos) {
        this.ctx = ctx;
        this.pos = new Vector2(pos);
        stateTime = 0;
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
    }

    @Override
    public void dispose() {
        ctx.removeEffect(this);
    }

    public Vector2 getPos() {
        return pos;
    }
}
