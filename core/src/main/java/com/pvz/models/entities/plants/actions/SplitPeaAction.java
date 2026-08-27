package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.SplitPeaConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileFactory;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.TileTags;

public class SplitPeaAction extends PlantAction {

    private final SplitPeaConfig config;
    private boolean hasFront;
    private boolean hasBack;
    private float animDuration;
    private boolean shotFront;
    private boolean shotBack1;
    private boolean shotBack2;
    private float cooldownTimer;

    public SplitPeaAction(SplitPeaConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        cooldownTimer += dt;
        if (cooldownTimer < config.intervalSeconds) return false;

        int plantCol = plant.getCol();
        int plantLane = plant.getLane();
        float range = GameMap.TILE_WIDTH * 1.6f;

        hasFront = false;
        hasBack = false;

        float plantX = GameController.colToWorldX(plantCol);

        for (var z : ctx.getZombiesInLane(plantLane)) {
            float dx = z.getX() - plantX;
            if (dx > 0 && dx < range) {
                hasFront = true;
            } else if (dx < 0) {
                hasBack = true;
            }
        }

        if (!hasFront) {
            for (int i = plantCol; i < ctx.getMap().getColumns(); i++) {
                var tags = ctx.getTileAt(i, plantLane).getTags();
                if (tags.contains(TileTags.GRAVE) || tags.contains(TileTags.ICE_BLOCK)) {
                    hasFront = true;
                    break;
                }
            }
        }

        if (!hasBack) {
            for (int i = 0; i < plantCol; i++) {
                var tags = ctx.getTileAt(i, plantLane).getTags();
                if (tags.contains(TileTags.GRAVE) || tags.contains(TileTags.ICE_BLOCK)) {
                    hasBack = true;
                    break;
                }
            }
        }

        return hasFront || hasBack;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0;
        cooldownTimer = 0;
        shotFront = false;
        shotBack1 = false;
        shotBack2 = false;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        String clip = currentClip();
        animDuration = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, clip);
        if (animDuration <= 0) animDuration = 0.6f;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        float hitTime = animDuration * 0.4f;

        if (!shotFront && hasFront && stateTime > hitTime) {
            spawnProjectile(ctx,
                    x + config.forwardOffsetX, y + config.forwardOffsetY,
                    config.projectileSpeed, 0, plant.getType());
            shotFront = true;
        }
        if (!shotBack1 && hasBack && stateTime > hitTime) {
            spawnProjectile(ctx,
                    x + config.backwardOffsetX, y + config.backwardOffsetY,
                    -config.projectileSpeed, 0, plant.getType());
            shotBack1 = true;
        }
        if (!shotBack2 && hasBack && stateTime > hitTime + 0.1f) {
            spawnProjectile(ctx,
                    x + config.backwardOffsetX, y + config.backwardOffsetY,
                    -config.projectileSpeed, 0, plant.getType());
            shotBack2 = true;
        }

        if (stateTime >= animDuration) {
            plant.changeState(new PlantIdleState());
        }
    }

    private void spawnProjectile(GameContext ctx, float x, float y, float velX, float velY, com.pvz.models.entities.plants.enums.PlantType plantType) {
        Projectile p = ProjectileFactory.create(
                ProjectileType.PEA, ctx,
                new Vector2(x, y), new Vector2(velX, velY),
                config.damage);
        p.setSourcePlantType(plantType);
        ctx.spawnProjectile(p);
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        hasFront = false;
        hasBack = false;
        shotFront = false;
        shotBack1 = false;
        shotBack2 = false;
        stateTime = 0;
        cooldownTimer = 0;
    }

    private String currentClip() {
        if (hasFront && hasBack) return config.attackBothClip;
        if (hasFront) return config.attackFrontClip;
        return config.attackBackClip;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position = new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.7f, 0.7f);
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pam.pamFilePath, currentClip(), stateTime, position, scale, null, false);
    }

    @Override
    public String getLabel() {
        return "SplitPea[" + currentClip() + "]";
    }
}
