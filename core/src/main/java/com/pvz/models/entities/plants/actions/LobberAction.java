package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.LobberActionConfig;
import com.pvz.models.entities.plants.config.LobberActionConfig.LobPattern;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileFactory;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Config-driven attack for lobber plants. Loads its {@link LobberActionConfig}
 * payload the same way {@link ShooterAction} loads {@code ShooterActionConfig}:
 * each pattern is copied into a per-attack list and spawned once its delay elapses.
 *
 * <p>Unlike a straight shooter, a lobbed projectile is aimed at a specific zombie
 * (the parabolic arc lands on it), so on every pattern the action picks a handful
 * of random zombies in the plant's lane — or every zombie when {@code allLanes} or
 * {@code targets <= 0} — and hands the target to {@link ProjectileFactory}, which
 * decides the arc, splash and status effects from the {@code projectileType}.
 */
public class LobberAction extends PlantAction {

    private final LobberActionConfig config;
    private final ArrayList<LobPattern> patterns = new ArrayList<>();
    private float animTime = 0f;

    public LobberAction(LobberActionConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;
        if (stateTime < config.intervalSeconds) {
            return false;
        }
        return !ctx.getZombiesInLane(plant.getLane()).isEmpty();
    }

    @Override
    public String getLabel() {
        return "Lob";
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        patterns.clear();
        for (LobberActionConfig.LobPattern pattern : config.patterns) {
            if (pattern.chance < 1f && Math.random() >= pattern.chance) {
                continue;
            }
            LobPattern newPattern = new LobberActionConfig.LobPattern();
            newPattern.projectileType = pattern.projectileType;
            newPattern.damage = pattern.damage;
            newPattern.targets = pattern.targets;
            newPattern.allLanes = pattern.allLanes;
            newPattern.positionOffset.set(pattern.positionOffset);
            newPattern.delaySeconds = pattern.delaySeconds;
            patterns.add(newPattern);
        }

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        animTime = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, config.label);
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);

        Iterator<LobberActionConfig.LobPattern> iterator = patterns.iterator();
        while (iterator.hasNext()) {
            LobberActionConfig.LobPattern pattern = iterator.next();
            if (stateTime >= pattern.delaySeconds) {
                spawnLobbedProjectiles(plant, ctx, pattern);
                iterator.remove();
            }
        }

        if (patterns.isEmpty() && stateTime >= animTime) {
            plant.changeState(new PlantIdleState());
        }
    }

    private void spawnLobbedProjectiles(Plant plant, GameContext ctx, LobberActionConfig.LobPattern pattern) {
        List<Zombie> candidates = pattern.allLanes
                ? ctx.getZombies().stream().filter(z -> !z.isDead()).toList()
                : ctx.getZombiesInLane(plant.getLane());

        int count = pattern.targets <= 0 ? candidates.size() : Math.min(pattern.targets, candidates.size());

        List<Zombie> targets = new ArrayList<>(candidates);
        Collections.shuffle(targets);

        float x = GameController.colToWorldX(plant.getCol()) + pattern.positionOffset.x;
        float y = GameController.laneToWorldY(plant.getLane()) + pattern.positionOffset.y;

        for (int i = 0; i < count; i++) {
            Zombie target = targets.get(i);
            Projectile projectile = ProjectileFactory.create(
                    pattern.projectileType, ctx, new Vector2(x, y), new Vector2(), pattern.damage, target);
            projectile.setSourcePlantType(plant.getType());
            ctx.spawnProjectile(projectile);
            ctx.log("[Action] " + plant.getSheet().getName() + " lobbed " + pattern.projectileType
                    + " at zombie in lane " + GameController.worldYtoLane(target.getY()) + ".");
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);
        return new FrameConfig(pam.pamFilePath, config.label, stateTime, pos, scale, null, true);
    }
}
