package com.pvz.models.entities.plants.actions;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.WallNutFeedConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.plants.fsm.WallNutState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.fsm.WalkState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

/**
 * Plant Food for wall-nut category plants, driven by {@link WallNutFeedConfig}.
 *
 * <p>Applies the configured effect on entry, plays the sheet's plant-food clip
 * sequence, then hands the plant back to {@link PlantIdleState} (and therefore
 * back to its {@code WallNutState}).
 */
public class WallNutFeedAction extends PlantAction {

    private final WallNutFeedConfig config;
    private float totalDuration;

    public WallNutFeedAction(WallNutFeedConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        applyEffect(plant, ctx);
        totalDuration = computeTotalDuration(plant);
    }

    private void applyEffect(Plant plant, GameContext ctx) {
        switch (config.kind) {
            case "FORCE_MOVE_ALL_IN_LANE" -> {
                for (Zombie zombie : ctx.getZombiesInLane(plant.getLane())) {
                    WallNutState.redirectToAdjacentLane(zombie, plant, ctx);
                }
            }
            case "FULL_HEAL_AND_ABSORB" -> {
                plant.heal(plant.getMaxHp());
                absorbNearbyZombies(plant, ctx);
            }
            default -> {
                if (config.amount > 0f) {
                    plant.boostMaxHp(config.amount);
                    ctx.log("[PlantFood] " + plant.getSheet().getName() + " gained " + (int) config.amount
                            + " permanent armor HP.");
                }
                if (config.reflectBonus > 0f) {
                    plant.addReflectDamageBonus(config.reflectBonus);
                    ctx.log("[PlantFood] " + plant.getSheet().getName() + " gained +"
                            + (int) config.reflectBonus + " reflect damage.");
                }
            }
        }
    }

    private void absorbNearbyZombies(Plant plant, GameContext ctx) {
        for (int adjacent : WallNutState.adjacentLanes(plant, ctx)) {
            for (Zombie zombie : ctx.getZombiesInLane(adjacent)) {
                if (zombie.getCurrentState() instanceof WalkState
                        && Math.abs(zombie.getX() - plant.getX()) <= 2f * Tile.WIDTH) {
                    WallNutState.moveZombieToLane(zombie, plant.getLane(), ctx);
                }
            }
        }
    }

    private float computeTotalDuration(Plant plant) {
        float total = 0f;
        for (String clip : config.clips) {
            total += clipDuration(plant, clip);
        }
        return total > 0f ? total : 0.5f;
    }

    private float clipDuration(Plant plant, String clip) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        if (pam == null || pam.pamFilePath == null || clip == null) {
            return -1f;
        }
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        if (catalog == null) {
            return -1f;
        }
        return catalog.getClipDuration(pam.pamFilePath, clip);
    }

    private String currentClip(Plant plant) {
        if (config.clips.length == 0) {
            PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
            return pam != null && pam.idleLabel != null ? pam.idleLabel : "idle";
        }
        float acc = 0f;
        for (String clip : config.clips) {
            float duration = clipDuration(plant, clip);
            if (stateTime < acc + duration || duration <= 0f) {
                return clip;
            }
            acc += duration;
        }
        return config.clips[config.clips.length - 1];
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime >= totalDuration) {
            plant.changeState(new PlantIdleState());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        Vector2 pos = new Vector2(GameController.colToWorldX(plant.getCol()), GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        return new FrameConfig(pam.pamFilePath, currentClip(plant), stateTime, pos, scale, null, true);
    }

    @Override
    public String getLabel() {
        return "PlantFood";
    }
}
