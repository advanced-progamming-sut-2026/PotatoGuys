package com.pvz.models.entities.plants.actions.explosive;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.ExplosiveConfig;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.effects.HotPotatoEffect;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.behaviors.IceBlockBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.tile.Tile;

public class HotPotatoAction extends PlantAction {
    private ExplosiveConfig config;
    private float velocity;
    private boolean effectsCreated;
    private float targetY;

    public HotPotatoAction(ExplosiveConfig config) {
        this.config = config;
        effectsCreated = false;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return true;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        targetY = plant.getPosition().y;
        plant.getPosition().add(0, GameMap.TILE_HEIGHT * 0.5f);

        float moveDuration = config.explosionTime / 3f;
        velocity = (targetY - plant.getPosition().y) / moveDuration;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime > config.explosionTime / 3f) {
            if (!effectsCreated && stateTime > config.explosionTime * 4f / 9f) {
                ctx.addEffect(new HotPotatoEffect(ctx, new Vector2(plant.getPosition().x, targetY)));
                effectsCreated = true;
            }
            if (stateTime < config.explosionTime * 2f / 3f) {
                plant.getPosition().add(0, velocity * dt);
            }
            if (stateTime > config.explosionTime) {
                Tile tile = ctx.getMap().getTileAt(plant.getCol(), plant.getLane());
                for (TileBehavior behavior : new ArrayList<>(tile.getBehaviors())) {
                    if (behavior instanceof IceBlockBehavior iceBlockBehavior) {
                        iceBlockBehavior.processHit(100000f);
                    }
                }
                plant.dispose();
            }
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        return;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath, config.explosionClip, stateTime, plant.getPosition(),
                new Vector2(0.65f, 0.65f), null, false);
    }

    @Override
    public String getLabel() {
        return null;
    }

}
