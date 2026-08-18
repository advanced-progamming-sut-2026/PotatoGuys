package com.pvz.models.entities.zombies.skills;

import java.util.ArrayList;
import java.util.List;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.projectile.OctopusProjectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.OctopusSkillConfig;
import com.pvz.models.entities.zombies.fsm.ZombieState;
import com.pvz.models.games.GameContext;

/**
 * Octopus Zombie — periodically throws an octopus projectile at the nearest
 * active (non-bound) plant. On landing, the octopus covers the plant,
 * disabling it and blocking the tile.
 *
 * <p>The skill fires on a repeating cooldown ({@code cooldownSeconds}). If
 * no active plants exist when the cooldown expires, the skill stays idle
 * until a valid target appears.
 */
public class OctopusBindingSkill extends CooldownSkill {

    private static final float THROW_DELAY = 1.4f;

    private final float octopusHp;
    private final float throwDuration;
    private final float arcHeight;

    private int pendingCol = -1;
    private int pendingLane = -1;
    private boolean thrown = false;

    public OctopusBindingSkill(OctopusSkillConfig config) {
        super(config, config.cooldownSeconds);
        this.octopusHp = config.octopusHp;
        this.throwDuration = config.throwDuration;
        this.arcHeight = config.arcHeight;
    }

    @Override
    protected boolean canUse(Zombie zombie, GameContext ctx) {
        return findTarget(zombie, ctx) != null;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        Plant target = findTarget(zombie, ctx);
        if (target == null) return;

        pendingCol = target.getCol();
        pendingLane = target.getLane();
        thrown = false;

        ctx.log("[Octopus] Zombie preparing to throw octopus at plant ("
            + pendingCol + "," + pendingLane + ")");
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        if (!thrown && stateTime >= THROW_DELAY) {
            thrown = true;
            Plant target = findTarget(zombie, ctx);
            if (target == null || target.getCol() != pendingCol || target.getLane() != pendingLane) {
                target = findTarget(zombie, ctx);
            }
            if (target != null) {
                float startX = zombie.getX();
                float startY = zombie.getY() + 40f;
                float targetX = GameController.colToWorldX(target.getCol());
                float targetY = GameController.laneToWorldY(target.getLane());

                OctopusProjectile projectile = new OctopusProjectile(
                    ctx, startX, startY, targetX, targetY,
                    target.getCol(), target.getLane(),
                    octopusHp, throwDuration, arcHeight
                );
                ctx.spawnOctopusProjectile(projectile);

                ctx.log("[Octopus] Zombie threw octopus at plant ("
                    + target.getCol() + "," + target.getLane() + ")");
            }
        }
        return super.update(zombie, ctx, dt);
    }

    @Override
    public String getName() {
        return "OctopusBinding";
    }

    /**
     * Finds the nearest active plant in the zombie's lane. Active means
     * not dead, not frozen, and not already bound by an octopus.
     */
    private Plant findTarget(Zombie zombie, GameContext ctx) {
        int lane = GameController.worldYtoLane(zombie.getY());
        int zombieCol = GameController.worldXtoCol(zombie.getX());
        Plant nearest = null;
        int bestDist = Integer.MAX_VALUE;

        for (Plant p : ctx.getPlants()) {
            if (p.getLane() != lane) continue;
            if (p.isDead() || p.isFrozen() || p.isBound()) continue;
            int dist = zombieCol - p.getCol();
            if (dist > 0 && dist < bestDist) {
                bestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }
}
