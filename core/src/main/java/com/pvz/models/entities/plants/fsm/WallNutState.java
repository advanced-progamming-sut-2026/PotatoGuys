package com.pvz.models.entities.plants.fsm;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.Effect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.NutConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.sun.SunType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.fsm.EatState;
import com.pvz.models.entities.zombies.fsm.WalkState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

/**
 * Behaviour state for wall-nut category plants (Wall-nut, Tall-nut, Endurian,
 * Garlic, Sweet Potato, Pumpkin, Sun Bean).
 *
 * <p>Takes over from {@link PlantIdleState} immediately on spawn so it can
 * switch between the sheet's idle / damage clips as the plant loses HP, and
 * runs the config-driven passive on-bite behaviours (Endurian reflect, Garlic
 * redirect, Sun Bean sun-on-hit, Sweet Potato attraction).
 *
 * <p>Explode-o-nut does not use this state — it keeps the existing
 * {@code ExplodeONutAction} so its detonation-on-destroy behaviour is reused.
 */
public class WallNutState extends PlantAction {

    private final NutConfig config;
    private String currentClip;
    private float biteTimer = 0f;
    private float attractTimer = 0f;

    public WallNutState(NutConfig config) {
        this.config = config;
        this.currentClip = config.idleClip;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return true;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        biteTimer = 0f;
        attractTimer = 0f;
        currentClip = config.idleClip;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        updateClip(plant);
        List<Zombie> eaters = eaters(plant, ctx);
        updateBiteBehaviours(plant, ctx, eaters, dt);
        updateAttract(plant, ctx, dt);
    }

    private void updateClip(Plant plant) {
        float maxHp = plant.getMaxHp();
        float fraction = maxHp <= 0f ? 1f : plant.getHp() / maxHp;
        if (fraction <= 0.25f) {
            currentClip = config.damage3Clip;
        } else if (fraction <= 0.5f) {
            currentClip = config.damage2Clip;
        } else if (fraction <= 0.75f) {
            currentClip = config.damage1Clip;
        } else {
            currentClip = config.idleClip;
        }
    }

    private List<Zombie> eaters(Plant plant, GameContext ctx) {
        List<Zombie> eaters = new ArrayList<>();
        for (Zombie zombie : ctx.getZombiesAt(plant.getCol(), plant.getLane())) {
            if (zombie.getCurrentState() instanceof EatState eatState && eatState.getTarget() == plant) {
                eaters.add(zombie);
            }
        }
        return eaters;
    }

    private void updateBiteBehaviours(Plant plant, GameContext ctx, List<Zombie> eaters, float dt) {
        if (eaters.isEmpty()) {
            biteTimer = 0f;
            return;
        }
        biteTimer += dt;
        if (biteTimer < 1f) {
            return;
        }
        float reflect = effectiveReflect(plant);
        if (reflect > 0f) {
            for (Zombie zombie : eaters) {
                zombie.takeDamage(reflect);
            }
        }
        if (config.redirectEaters) {
            for (Zombie zombie : eaters) {
                redirectToAdjacentLane(zombie, plant, ctx);
            }
        }
        int sunAmount = effectiveSunOnHit(plant);
        if (sunAmount > 0) {
            Sun sun = new Sun(SunType.NORMAL, plant.getCol(), plant.getLane(), sunAmount, false, ctx);
            ctx.spawnSun(sun);
            ctx.log("[WallNut] " + plant.getSheet().getName() + " dropped " + sunAmount + " sun.");
        }
        biteTimer = 0f;
    }

    /** Base reflect damage + permanent plant-food bonus + the "Reflect Dmg +5" level flag. */
    private float effectiveReflect(Plant plant) {
        float reflect = config.reflectDamage + plant.getReflectDamageBonus();
        if (plant.getUnlockedFlags().contains("Reflect Dmg +5")) {
            reflect += 5f;
        }
        return reflect;
    }

    /** Base sun-per-bite + the "Sun Drop +5" level flag. */
    private int effectiveSunOnHit(Plant plant) {
        int sun = config.sunOnHit;
        if (plant.getUnlockedFlags().contains("Sun Drop +5")) {
            sun += 5;
        }
        return sun;
    }

    private void updateAttract(Plant plant, GameContext ctx, float dt) {
        if (!config.attractAdjacent) {
            return;
        }
        attractTimer += dt;
        if (attractTimer < 1f) {
            return;
        }
        attractTimer = 0f;
        for (int adjacent : adjacentLanes(plant, ctx)) {
            for (Zombie zombie : ctx.getZombiesInLane(adjacent)) {
                if (zombie.getCurrentState() instanceof WalkState
                        && Math.abs(zombie.getX() - plant.getX()) <= 2f * Tile.WIDTH) {
                    moveZombieToLane(zombie, plant.getLane(), ctx);
                }
            }
        }
    }

    /** The lane one step above/below the plant's own (skipping out-of-bounds). */
    public static int[] adjacentLanes(Plant plant, GameContext ctx) {
        int lanes = ctx.getMap().getLanes();
        int above = plant.getLane() - 1;
        int below = plant.getLane() + 1;
        boolean hasAbove = above >= 0;
        boolean hasBelow = below < lanes;
        if (hasAbove && hasBelow) {
            return new int[] { above, below };
        }
        if (hasAbove) {
            return new int[] { above };
        }
        if (hasBelow) {
            return new int[] { below };
        }
        return new int[0];
    }

    /** Moves a zombie into {@code lane}; chewing zombies are forced back to walking. */
    public static void moveZombieToLane(Zombie zombie, int lane, GameContext ctx) {
        if (zombie.getCurrentState() instanceof EatState) {
            zombie.setState(new WalkState());
        }
        zombie.setY(GameController.laneToWorldY(lane));
    }

    /** Redirects an eater to an adjacent lane (Garlic passive / plant food). */
    public static void redirectToAdjacentLane(Zombie zombie, Plant plant, GameContext ctx) {
        int[] adjacent = adjacentLanes(plant, ctx);
        if (adjacent.length == 0) {
            return;
        }
        moveZombieToLane(zombie, adjacent[0], ctx);
        ctx.log("[WallNut] Garlic redirected " + zombie.getSheet().getAlias() + " to lane " + adjacent[0] + ".");
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

        return new FrameConfig(pam.pamFilePath, currentClip, stateTime, pos, scale, plant.getArmorPartsVisibility(), true);
    }

    @Override
    public String getLabel() {
        return "WallNut[" + currentClip + "]";
    }
}
