package com.pvz.models.entities.plants.actions.explosive.potato_mine;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.entities.effects.Explosion;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

public class PotatoMineExplosionAction extends PlantAction {
    String pamPath;
    String clip;
    float explosionDuration;
    ExplosionType type;
    ExplosionIntensity intensity;
    Zombie target;
    public PotatoMineExplosionAction(String pamPath, String clip, float explosionDuration
        , ExplosionType type, ExplosionIntensity intensity, Zombie target){
        this.pamPath=pamPath;
        this.clip=clip;
        this.explosionDuration=explosionDuration;
        this.intensity=intensity;
        this.type=type;
        this.target=target;
    }
    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>=explosionDuration){
            ctx.addEffect(new Explosion(ctx,type,intensity,plant.getPosition()));
            target.takeDamage(1800);
            plant.dispose();
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        return new FrameConfig(pamPath,clip,stateTime,position,scale,null,false);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
