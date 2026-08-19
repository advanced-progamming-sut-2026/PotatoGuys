package com.pvz.controller.game.State;

import com.pvz.controller.game.GameController;

public class PlantSelect extends State {

    public PlantSelect(GameController controller) {
        super(controller);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        controller.getCamera().position.set(controller.getEndX(), 720f / 2f, 0);
    }

}
