package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.Comparator;
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

public class ChomperFeedAction extends PlantAction {

    private enum Phase { ON, PULLING, OFF, BURP, BURP_END }

    private final ChomperConfig config;
    private Phase phase;
    private float phaseTimer;
    private List<Zombie> pulledZombies;
    private float plantX;
    private float plantY;
    private float onDuration;
    private float pullingDuration;
    private float offDuration;
    private float burpDuration;
    private float burpEndDuration;
    private boolean killDone;

    public ChomperFeedAction(ChomperConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return true;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0;
        phaseTimer = 0;
        phase = Phase.ON;
        killDone = false;
        plantX = GameController.colToWorldX(plant.getCol());
        plantY = GameController.laneToWorldY(plant.getLane());

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        String pamPath = pam.pamFilePath;
        onDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.pfOnClip);
        pullingDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.pfClip);
        offDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.pfOffClip);
        burpDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.pfBurpClip);
        burpEndDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.pfBurpEndClip);

        if (onDuration <= 0) onDuration = 0.5f;
        if (pullingDuration <= 0) pullingDuration = 1.5f;
        if (offDuration <= 0) offDuration = 0.5f;
        if (burpDuration <= 0) burpDuration = 0.5f;
        if (burpEndDuration <= 0) burpEndDuration = 0.5f;

        int plantLane = plant.getLane();
        float range = config.biteRangeFactor * GameMap.TILE_WIDTH;

        List<Zombie> candidates = new ArrayList<>();
        for (Zombie z : ctx.getZombiesInLane(plantLane)) {
            float dx = z.getX() - plantX;
            float dy = Math.abs(z.getY() - plantY);
            if (dx > 0 && dx < range * 3 && dy < range) {
                candidates.add(z);
            }
        }
        candidates.sort(Comparator.comparingDouble(z -> z.getX() - plantX));

        pulledZombies = new ArrayList<>();
        for (int i = 0; i < Math.min(config.pfMaxTargets, candidates.size()); i++) {
            pulledZombies.add(candidates.get(i));
        }
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        phaseTimer += dt;

        switch (phase) {
            case ON:
                if (phaseTimer >= onDuration) {
                    phase = Phase.PULLING;
                    phaseTimer = 0;
                }
                break;

            case PULLING:
                for (Zombie z : pulledZombies) {
                    if (z != null && !z.isDead()) {
                        float dx = plantX - z.getX();
                        if (Math.abs(dx) > 5) {
                            float dirX = dx > 0 ? 1 : -1;
                            z.setX(z.getX() + dirX * config.pfPullSpeed * dt);
                        }
                    }
                }

                if (!killDone && phaseTimer > pullingDuration * 0.7f) {
                    for (Zombie z : pulledZombies) {
                        if (z != null && !z.isDead()) {
                            z.takeDamage(99999);
                        }
                    }
                    killDone = true;
                }

                if (phaseTimer >= pullingDuration) {
                    phase = Phase.OFF;
                    phaseTimer = 0;
                }
                break;

            case OFF:
                if (phaseTimer >= offDuration) {
                    phase = Phase.BURP;
                    phaseTimer = 0;
                }
                break;

            case BURP:
                if (phaseTimer >= burpDuration) {
                    phase = Phase.BURP_END;
                    phaseTimer = 0;
                }
                break;

            case BURP_END:
                if (phaseTimer >= burpEndDuration) {
                    plant.changeState(new PlantIdleState());
                }
                break;
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        pulledZombies = null;
        phase = Phase.ON;
        phaseTimer = 0;
        stateTime = 0;
        killDone = false;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position = new Vector2(plantX, plantY);
        Vector2 scale = new Vector2(0.65f, 0.65f);
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;

        String clip;
        boolean looping;
        switch (phase) {
            case ON:
                clip = config.pfOnClip;
                looping = false;
                break;
            case PULLING:
                clip = config.pfClip;
                looping = true;
                break;
            case OFF:
                clip = config.pfOffClip;
                looping = false;
                break;
            case BURP:
                clip = config.pfBurpClip;
                looping = false;
                break;
            case BURP_END:
                clip = config.pfBurpEndClip;
                looping = false;
                break;
            default:
                clip = config.pfOnClip;
                looping = false;
                break;
        }

        return new FrameConfig(pam.pamFilePath, clip, phaseTimer, position, scale, null, looping);
    }

    @Override
    public String getLabel() {
        return "ChomperFeed[" + (phase != null ? phase.name() : "?") + "]";
    }
}
