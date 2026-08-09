package com.pvz.models.entities.plants.actions.shooters;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.CooldownPlantAction;
import com.pvz.models.entities.plants.config.ShooterActionConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig.ProjectilePattern;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.models.games.GameContext;

/**
 * Data-driven shooter behavior: fires every {@link ShooterActionConfig.ProjectilePattern}
 * declared on its config (see {@code plant_actions.json}), each pattern independently
 * offset/aimed/delayed and optionally broadcast to every lane ({@code allLanes}).
 */
public class ShooterAction extends CooldownPlantAction {

    private final ShooterActionConfig config;

    public ShooterAction(float intervalSeconds, ShooterActionConfig config) {
        super(intervalSeconds);
        this.config = config;
    }

    @Override
    protected boolean canUse(Plant plant, GameContext ctx) {
        return !ctx.getZombiesInLane(plant.getLane()).isEmpty();
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        fire(config, plant, ctx);
    }

    @Override
    public String getName() {
        return "Shoot";
    }

    /** Fires every pattern in {@code config} once, honoring per-pattern delay/lane-broadcast. Reused by Plant Food. */
    public static void fire(ShooterActionConfig config, Plant plant, GameContext ctx) {
        boolean pierceThrough = plant.getSheet().getCategory() == PlantCategory.STRIKE_THROUGH;
        for (ProjectilePattern pattern : config.patterns) {
            Runnable spawn = () -> spawnPattern(pattern, plant, ctx, pierceThrough);
            int delayTicks = Math.round(pattern.delaySeconds * Plant.TICKS_PER_SECOND);
            if (delayTicks <= 0) {
                spawn.run();
            } else {
                ctx.getEngine().register(new DelayedShot(delayTicks, ctx, spawn));
            }
        }
    }

    private static void spawnPattern(ProjectilePattern pattern, Plant plant, GameContext ctx, boolean pierceThrough) {
        if (pattern.allLanes) {
            for (int lane = 0; lane < ctx.getLanes(); lane++) {
                spawnAt(pattern, plant, lane, ctx, pierceThrough);
            }
        } else {
            int lane = plant.getLane() + pattern.laneOffset;
            if (lane < 0 || lane >= ctx.getLanes()) return;
            spawnAt(pattern, plant, lane, ctx, pierceThrough);
        }
    }

    private static void spawnAt(ProjectilePattern pattern, Plant plant, int lane, GameContext ctx, boolean pierceThrough) {
        float col = plant.getCol() + pattern.positionOffset.x;
        float row = lane + pattern.positionOffset.y;
        boolean ice = pattern.projectileType == ProjectileType.SNOW_PEA;
        boolean fire = pattern.projectileType == ProjectileType.FIRE_PEA;
        boolean poison = pattern.projectileType == ProjectileType.GOO_PEA;
        int pierceCount = pierceThrough ? 3 : 0;

        Projectile projectile = new Projectile(ctx, pattern.projectileType, row, col, pattern.damage,
                poison, ice, fire, pierceCount, null);
        projectile.setVelocityVector(pattern.velocity.x, pattern.velocity.y);
        ctx.spawnProjectile(projectile);
    }
}

