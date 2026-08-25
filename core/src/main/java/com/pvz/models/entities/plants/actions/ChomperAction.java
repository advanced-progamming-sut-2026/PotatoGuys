package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.ChomperConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

public class ChomperAction extends PlantAction {

    private enum Phase { BITE, SPECIAL, DIGEST, END }

    private final ChomperConfig config;
    private Phase phase;
    private Zombie target;
    private float phaseTimer;
    private float biteDuration;
    private float specialDuration;
    private float endDuration;

    public ChomperAction(ChomperConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        int plantCol = plant.getCol();
        int plantLane = plant.getLane();
        float range = config.biteRangeFactor * GameMap.TILE_WIDTH;

        for (Zombie z : ctx.getZombiesInLane(plantLane)) {
            float dx = z.getX() - GameController.colToWorldX(plantCol);
            float dy = Math.abs(z.getY() - GameController.laneToWorldY(plantLane));
            if (dx > 0 && dx < range && dy < range) {
                target = z;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0;
        phaseTimer = 0;
        phase = Phase.BITE;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        String pamPath = pam.pamFilePath;
        biteDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.biteClip);
        specialDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.specialClip);
        endDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.digestEndClip);

        if (biteDuration <= 0) biteDuration = 0.5f;
        if (specialDuration <= 0) specialDuration = 0.5f;
        if (endDuration <= 0) endDuration = 0.5f;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        phaseTimer += dt;

        switch (phase) {
            case BITE:
                if (phaseTimer > biteDuration * 0.5f && target != null && !target.isDead()) {
                    target.takeDamage(99999);
                    target = null;
                }
                if (phaseTimer >= biteDuration) {
                    phase = Phase.SPECIAL;
                    phaseTimer = 0;
                }
                break;

            case SPECIAL:
                if (phaseTimer >= specialDuration) {
                    phase = Phase.DIGEST;
                    phaseTimer = 0;
                }
                break;

            case DIGEST:
                if (phaseTimer >= config.digestionSeconds) {
                    phase = Phase.END;
                    phaseTimer = 0;
                }
                break;

            case END:
                if (phaseTimer >= endDuration) {
                    plant.changeState(new PlantIdleState());
                }
                break;
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        target = null;
        phase = Phase.BITE;
        phaseTimer = 0;
        stateTime = 0;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position = new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;

        String clip;
        boolean looping;
        switch (phase) {
            case BITE:
                clip = config.biteClip;
                looping = false;
                break;
            case SPECIAL:
                clip = config.specialClip;
                looping = false;
                break;
            case DIGEST:
                clip = config.digestClip;
                looping = true;
                break;
            case END:
                clip = config.digestEndClip;
                looping = false;
                break;
            default:
                clip = config.biteClip;
                looping = false;
                break;
        }

        return new FrameConfig(pam.pamFilePath, clip, phaseTimer, position, scale, null, looping);
    }

    @Override
    public String getLabel() {
        return "Chomper[" + (phase != null ? phase.name() : "?") + "]";
    }
}
