package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig.ProjectilePattern;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileFactory;
import com.pvz.models.games.GameContext;

public class ShooterAction extends PlantAction {

    private final ShooterActionConfig config;
    public ArrayList<ProjectilePattern> patterns = new ArrayList<>();

    public ShooterAction(ShooterActionConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx , float dt) {
        stateTime += dt;
        if(stateTime < config.intervalSeconds) {
            return false;
        }
        return ctx.getZombiesInLane(plant.getLane()).stream()
                .anyMatch(zombie -> zombie.getX() >= plant.getCol());
    }


    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        patterns.clear();
        for(ShooterActionConfig.ProjectilePattern pattern : config.patterns) {
            ProjectilePattern newPattern = new ShooterActionConfig.ProjectilePattern();
            newPattern.projectileType = pattern.projectileType;
            newPattern.damage = pattern.damage;
            newPattern.laneOffset = pattern.laneOffset;
            newPattern.allLanes = pattern.allLanes;
            newPattern.positionOffset.set(pattern.positionOffset);
            newPattern.velocity.set(pattern.velocity);
            newPattern.delaySeconds = pattern.delaySeconds;
            patterns.add(newPattern);
        }
    }

        @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        
        Iterator<ShooterActionConfig.ProjectilePattern> iterator = patterns.iterator();
        
        while (iterator.hasNext()) {
            
            ShooterActionConfig.ProjectilePattern pattern = iterator.next();
            
            if (stateTime >= pattern.delaySeconds) {
                spawnProjectile(plant, ctx, pattern);
                iterator.remove(); 
            }
        }

        if(patterns.isEmpty()) {
            plant.changeState(new PlantIdleState());
        }
    }

    private void spawnProjectile(Plant plant, GameContext ctx, ShooterActionConfig.ProjectilePattern pattern) {
        float x = (float)plant.getCol() + pattern.positionOffset.x;
        float y = (float)plant.getLane() + pattern.positionOffset.y;
        float velX = pattern.velocity.x;
        float velY = pattern.velocity.y;
        Projectile projectile = ProjectileFactory.create(pattern.projectileType, ctx, new Vector2(x, y), new Vector2(velX, velY), plant.getEffectiveDamage()); 
        ctx.spawnProjectile(projectile);
    }


    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // stateTime = 0f;
    }

    @Override
    public void draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        PvZ2.pamPlayer.draw(PvZ2.batch, config.pamFilePath , config.attackActionLabel, stateTime, x, y, true);
    }

}

