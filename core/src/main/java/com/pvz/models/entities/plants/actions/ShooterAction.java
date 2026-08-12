package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig.ProjectilePattern;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileFactory;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.TileTags;

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

        boolean laneHasZombie= ctx.getZombiesInLane(plant.getLane()).stream()
            .anyMatch(zombie -> zombie.getX() >= plant.getCol());

        boolean laneHasGraveOrIce=false;
        for (int i = plant.getCol(); i < ctx.getMap().getColumns(); i++) {
            List<TileTags> tileTags=ctx.getTileAt(i,plant.getLane()).getTags();
            if (tileTags.contains(TileTags.GRAVE) || tileTags.contains(TileTags.ICE_BLOCK)){
                laneHasGraveOrIce=true;
            }
        }

        return laneHasZombie || laneHasGraveOrIce;
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
        float x = GameController.colToWorldX(plant.getCol()) + pattern.positionOffset.x;
        float y = GameController.laneToWorldY(plant.getLane()) + pattern.positionOffset.y;
        float velX = pattern.velocity.x;
        float velY = pattern.velocity.y;
        Projectile projectile = ProjectileFactory.create(pattern.projectileType, ctx, new Vector2(x, y), new Vector2(velX, velY), plant.getEffectiveDamage());
        ctx.spawnProjectile(projectile);
        ctx.log("projectile spawned at x= "+x+"  y= "+y+" (col: "+GameController.worldXtoCol(x)+" lane: "+GameController.worldYtoLane(y)+")");
    }


    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // stateTime = 0f;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x,y);
        Vector2 scale = new Vector2(0.7f,0.7f);
        return new FrameConfig(config.pamFilePath,config.attackActionLabel,stateTime,pos,scale,
            null,true);
    }

}

