package com.pvz.models.entities.plants.actions;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.PassiveConfig;
import com.pvz.models.games.GameContext;

/**
 * No-op action for passive / modifier plants.  The plant simply stands
 * idle and plays its default animation.  Plant Food is handled as a
 * separate feed action when needed.
 */
public class PassiveAction extends PlantAction {

    private final PassiveConfig config;

    public PassiveAction(PassiveConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) { }

    @Override
    public void onExit(Plant plant, GameContext ctx) { }

    @Override
    public String getLabel() { return "Passive"; }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);
        String label = plant.getSheet().pamAnimationConfig.idleLabel;
        return new FrameConfig(plant.getSheet().pamAnimationConfig.pamFilePath,
                label, stateTime, pos, scale, null, true);
    }
}
