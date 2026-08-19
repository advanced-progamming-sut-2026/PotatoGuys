package com.pvz.controller.game.State;

import com.pvz.controller.game.GameController;

public class Playing extends State {

    public Playing(GameController controller) {
        super(controller);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        controller.getCamera().position.set(controller.getStartX(), 720f / 2f, 0);
        controller.getGameUiModal().updateHud();
        if (controller.getCtx().isGameOver() && !controller.isPaused()) {
            controller.showGameEndPopup();
        }
    }

}
