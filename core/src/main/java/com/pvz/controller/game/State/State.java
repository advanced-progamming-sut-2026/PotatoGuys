package com.pvz.controller.game.State;

import com.pvz.controller.game.GameController;

public abstract class State {
    protected GameController controller;
    protected float stateTime;

    public State(GameController controller) {
        this.controller = controller;
    }

    public void enter() {

    }

    public void update(float dt) {
        stateTime += dt;
    }

    public void exit() {

    }
}
