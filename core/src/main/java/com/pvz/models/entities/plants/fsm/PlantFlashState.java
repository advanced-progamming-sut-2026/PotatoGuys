package com.pvz.models.entities.plants.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

/**
 * Temporary damage-flash wrapper around any {@link PlantState}.
 * Delegates all logic to the wrapped state while applying a white
 * semi-transparent tint to the draw output for {@code FLASH_DURATION} seconds.
 */
public class PlantFlashState extends PlantState {

    private static final float FLASH_DURATION = 0.2f;
    private static final float FLASH_R = 5f;
    private static final float FLASH_G = 5f;
    private static final float FLASH_B = 5f;
    private static final float FLASH_A = 0.6f;

    private PlantState underlying;

    public PlantFlashState(PlantState underlying) {
        this.underlying = underlying;
        this.stateTime = 0f;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;
        underlying.update(plant, ctx, dt);
        if (stateTime >= FLASH_DURATION) {
            plant.setCurrentState(underlying);
            return;
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // underlying state is still alive — do NOT call its onExit
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        FrameConfig fc = underlying.draw(plant, ctx);
        if (fc != null) {
            fc.setColor(FLASH_R, FLASH_G, FLASH_B, FLASH_A);
        }
        return fc;
    }

    @Override
    public String getLabel() {
        return "Flash(" + underlying.getLabel() + ")";
    }
}
