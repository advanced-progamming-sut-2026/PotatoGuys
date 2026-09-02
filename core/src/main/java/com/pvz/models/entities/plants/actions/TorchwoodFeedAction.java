package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.TorchwoodHitEffect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.TorchwoodConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Torchwood plant food: plays {@code plantfood_on_t2} once, then
 * {@code plantfood_t2}, dealing one hit to every zombie on the board (each with
 * a TORCHWOOD_HIT_EFFECTS clip) before returning to idle.
 */
public class TorchwoodFeedAction extends PlantAction {

    private final TorchwoodConfig config;
    private boolean onClipPlayed;
    private float animTime;
    private boolean dealt;

    public TorchwoodFeedAction(TorchwoodConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        onClipPlayed = false;
        dealt = false;
        animTime = clipDuration(plant, config.plantFoodOnClip);
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;

        if (!onClipPlayed) {
            if (stateTime >= animTime) {
                onClipPlayed = true;
                stateTime = 0f;
                animTime = clipDuration(plant, config.plantFoodClip);
            }
            return;
        }

        if (dealt) {
            if (stateTime >= animTime) {
                plant.changeState(new PlantIdleState());
            }
            return;
        }

        // Hit every zombie on the field once when the main plant-food clip starts.
        dealt = true;
        for (Zombie z : new ArrayList<>(ctx.getZombies())) {
            if (z.isDead()) {
                continue;
            }
            z.takeDamage(config.plantFoodDamage);
            ctx.addEffect(new TorchwoodHitEffect(ctx, z.getPosition(), config.plantFoodHitClip));
        }
    }

    private float clipDuration(Plant plant, String clip) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        float d = catalog != null ? catalog.getClipDuration(pam.pamFilePath, clip) : -1f;
        return d > 0f ? d : 0.7f;
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        Vector2 pos = new Vector2(GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.7f, 0.7f);
        String clip = onClipPlayed ? config.plantFoodClip : config.plantFoodOnClip;
        return new FrameConfig(pam.pamFilePath, clip, stateTime, pos, scale, null, false);
    }

    @Override
    public String getLabel() {
        return "TorchwoodFeed";
    }
}