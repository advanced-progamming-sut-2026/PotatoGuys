package com.pvz.controller.game.State;

import com.pvz.controller.game.GameController;

public class PanningBack extends State {
    private final float PAN_BACK_DURATION = 2.5f;

    public PanningBack(GameController controller) {
        super(controller);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        stateTime += dt;
        float progressBack = Math.min(1f, stateTime / PAN_BACK_DURATION);
        float smoothBack = com.badlogic.gdx.math.Interpolation.fade.apply(progressBack);
        float currentXBack = com.badlogic.gdx.math.MathUtils.lerp(
                controller.getEndX(), controller.getStartX(), smoothBack);
        controller.getCamera().position.set(currentXBack, 720f / 2f, 0);

        if (progressBack >= 1f) {
            // Camera is back in place — the decorative intro zombies vanish now.
            controller.clearDisplayZombies();
            controller.changeState(new ReadyPlant(controller));
            controller.getReadyPlantLabel().setVisible(true);
        }
    }

}
