package com.pvz.controller.game.State;

import com.badlogic.gdx.Gdx;
import com.pvz.controller.game.GameController;

public class PanningForward extends State {
    private final float TRANSITION_DURATION = 3.0f;

    public PanningForward(GameController controller) {
        super(controller);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        float progressFwd = Math.min(1f, stateTime / TRANSITION_DURATION);
        float smoothFwd = com.badlogic.gdx.math.Interpolation.fade.apply(progressFwd);
        float currentXFwd = com.badlogic.gdx.math.MathUtils.lerp(controller.getStartX(), controller.getEndX(),
                smoothFwd);
        controller.getCamera().position.set(currentXFwd, 720f / 2f, 0);

        if (progressFwd >= 1f) {
            if (controller.getLevel() != null && !controller.getLevel().hasPreGame()) {
                controller.startGameSession();
            } else {
                controller.changeState(new PlantSelect(controller));
                controller.getPlantSelectModal().setVisible(true);
                Gdx.input.setInputProcessor(controller.getStage());
            }
        }
    }

}
