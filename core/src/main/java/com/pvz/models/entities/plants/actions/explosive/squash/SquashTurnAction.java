package com.pvz.models.entities.plants.actions.explosive.squash;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.SquashConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

public class SquashTurnAction extends PlantAction {
    SquashConfig config;
    Zombie target;

    public SquashTurnAction(SquashConfig config, Zombie target) {
        this.config = config;
        this.target = target;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime >= config.turnDuration) {
            plant.changeState(new SquashJumpAction(config, target, false));
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 scale = new Vector2(0.65f, 0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath, config.turnClip, stateTime, plant.getPosition(), scale,
                null, true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
