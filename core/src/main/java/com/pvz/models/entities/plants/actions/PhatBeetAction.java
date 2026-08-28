package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.PhatBeetPulseEffect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.PhatBeetConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class PhatBeetAction extends PlantAction {
    private final PhatBeetConfig config;
    private final boolean feedMode;
    private List<Zombie> targets;
    private boolean attacked;
    private float animDuration;
    private float cooldownTimer;

    public PhatBeetAction(PhatBeetConfig config, boolean feedMode) {
        this.config = config;
        this.feedMode = feedMode;
    }

    private int currentRadius() {
        return feedMode ? config.pfRadius : config.attackRadius;
    }

    private float currentDamage() {
        return feedMode ? config.pfDamage : config.baseDamage;
    }

    private float currentInterval() {
        return feedMode ? config.pfIntervalSeconds : config.intervalSeconds;
    }

    private String currentEffectPamPath() {
        return feedMode ? config.pfEffectPamPath : config.effectPamPath;
    }

    private String currentEffectClip() {
        return feedMode ? config.pfEffectClip : config.effectClip;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        if (feedMode) return true;

        cooldownTimer += dt;
        if (cooldownTimer < config.intervalSeconds) return false;

        int plantCol = plant.getCol();
        int plantLane = plant.getLane();
        float range = config.attackRangeFactor * GameMap.TILE_WIDTH;
        int radius = currentRadius();

        targets = new ArrayList<>();
        for (int lane = plantLane - radius; lane <= plantLane + radius; lane++) {
            for (int col = plantCol - radius; col <= plantCol + radius; col++) {
                for (Zombie z : ctx.getZombiesInLane(lane)) {
                    float dx = Math.abs(z.getX() - GameController.colToWorldX(col));
                    float dy = Math.abs(z.getY() - GameController.laneToWorldY(lane));
                    if (dx < range && dy < range) {
                        targets.add(z);
                    }
                }
            }
        }

        Tile frontTile = ctx.getMap().getTileAt(plantCol + 1, plantLane);
        boolean frontTileHasDestructible = frontTile != null
                && (frontTile.getTags().contains(TileTags.GRAVE) || frontTile.getTags().contains(TileTags.ICE_BLOCK));

        return !targets.isEmpty() || frontTileHasDestructible;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        attacked = false;
        stateTime = 0;
        cooldownTimer = 0;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        animDuration = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, config.label);
        if (animDuration <= 0) animDuration = currentInterval();

        if (feedMode) {
            int plantCol = plant.getCol();
            int plantLane = plant.getLane();
            float range = config.attackRangeFactor * GameMap.TILE_WIDTH;
            int radius = currentRadius();

            targets = new ArrayList<>();
            for (int lane = plantLane - radius; lane <= plantLane + radius; lane++) {
                for (int col = plantCol - radius; col <= plantCol + radius; col++) {
                    for (Zombie z : ctx.getZombiesInLane(lane)) {
                        float dx = Math.abs(z.getX() - GameController.colToWorldX(col));
                        float dy = Math.abs(z.getY() - GameController.laneToWorldY(lane));
                        if (dx < range && dy < range) {
                            targets.add(z);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        float hitTime = 0.53f;

        if (!attacked && stateTime > hitTime) {
            float damage = currentDamage();
            int radius = currentRadius();
            int plantCol = plant.getCol();
            int plantLane = plant.getLane();

            if (targets != null) {
                for (Zombie z : targets) {
                    z.takeDamage(damage);
                }
            }

            for (int lane = plantLane - radius; lane <= plantLane + radius; lane++) {
                for (int col = plantCol - radius; col <= plantCol + radius; col++) {
                    Tile tile = ctx.getMap().getTileAt(col, lane);
                    if (tile != null) tile.processHit(damage);
                }
            }

            Vector2 plantPos = new Vector2(
                    GameController.colToWorldX(plant.getCol()),
                    GameController.laneToWorldY(plant.getLane()));
            ctx.addEffect(new PhatBeetPulseEffect(ctx, plantPos,
                    currentEffectPamPath(), currentEffectClip(), config.effectScale));
            ctx.addEffect(new PhatBeetPulseEffect(ctx, plantPos,
                    config.tileHitPamPath, config.tileHitClip, config.tileHitScale));

            attacked = true;
        }

        if (stateTime >= animDuration) {
            plant.changeState(new PlantIdleState());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        targets = null;
        attacked = false;
        stateTime = 0;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position = new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath, config.label, stateTime, position, scale, null, true);
    }

    @Override
    public String getLabel() {
        return feedMode ? "PhatBeetFeed" : "PhatBeetAttack";
    }
}
