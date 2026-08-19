package com.pvz.controller.game.State;

import com.pvz.controller.AudioManager;
import com.pvz.controller.game.GameController;
import com.pvz.enums.AudioPaths;

public class ReadyPlant extends State {
    private final float READY_PLANT_DURATION = 2.0f;

    public ReadyPlant(GameController controller) {
        super(controller);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        controller.getCamera().position.set(controller.getStartX(), 720f / 2f, 0);
        stateTime += dt;
        if (stateTime >= READY_PLANT_DURATION) {
            controller.getReadyPlantLabel().setVisible(false);
            controller.changeState(new Playing(controller));
            controller.setGameStarted(true);
            AudioManager audioManager = AudioManager.getInstance();
            if (controller.getLevel().getMusicPath() != null) {
                audioManager.playMusic(controller.getLevel().getMusicPath(), true, audioManager.getUserMusicVolume());
            } else {
                audioManager.playMusic(AudioPaths.GRASS_WALK, true, audioManager.getUserMusicVolume());
            }
        }
    }

}
