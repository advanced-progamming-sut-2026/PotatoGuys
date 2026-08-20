package com.pvz.models.entities.plants.actions.explosive.squash;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.SquashConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

public class SquashJumpAction extends PlantAction {
    SquashConfig config;
    Zombie target;
    boolean facingRight;
    String currentClip;
    Vector2 targetUpPos;
    Vector2 targetDownPos;
    Vector2 upVelocity;
    Vector2 downVelocity;
    boolean jumpUpOver;
    public SquashJumpAction(SquashConfig config, Zombie target, boolean facingRight){
        this.config=config;
        this.target=target;
        this.facingRight=facingRight;

        if (facingRight) currentClip=config.jumpUpRightClip;
        else currentClip=config.jumpUpLeftClip;

        jumpUpOver=false;
    }
    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        int col = GameController.worldXtoCol(target.getX());
        int lane = GameController.worldYtoLane(target.getY());

        float jumpHeight = plant.getPosition().y+ GameMap.TILE_HEIGHT * config.jumpHeightCoefficient;
        if (plant.getCol() == col && plant.getLane() == lane){
            targetUpPos=new Vector2(plant.getPosition().x, jumpHeight);
            targetDownPos=new Vector2(plant.getPosition());
        } else if(facingRight){
            targetUpPos=new Vector2(plant.getPosition().x + GameMap.TILE_WIDTH, jumpHeight);
            targetDownPos=new Vector2(plant.getPosition().x + GameMap.TILE_WIDTH, plant.getPosition().y);
        } else {
            targetUpPos=new Vector2(plant.getPosition().x - GameMap.TILE_WIDTH, jumpHeight);
            targetDownPos=new Vector2(plant.getPosition().x - GameMap.TILE_WIDTH, plant.getPosition().y);
        }

        float upVelocityX = (targetUpPos.x-plant.getPosition().x)/config.jumpUpDuration;
        float upVelocityY = (targetUpPos.y - plant.getPosition().y)/config.jumpUpDuration;
        upVelocity=new Vector2(upVelocityX,upVelocityY);

        float downVelocityX = (targetDownPos.x-targetUpPos.x)/config.jumpDownDuration;
        float downVelocityY = (targetDownPos.y - targetUpPos.y)/config.jumpDownDuration;
        downVelocity=new Vector2(downVelocityX,downVelocityY);
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime<config.jumpUpDuration && !jumpUpOver){
            plant.getPosition().x+=upVelocity.x*dt;
            plant.getPosition().y+=upVelocity.y*dt;
        } else if (stateTime >= config.jumpUpDuration && !jumpUpOver){
            jumpUpOver=true;
            stateTime=0;

            if (facingRight) currentClip=config.jumpDownRightClip;
            else currentClip = config.jumpDownLeftClip;
        }

        if (stateTime<config.jumpDownDuration && jumpUpOver){
            plant.getPosition().x+=downVelocity.x*dt;
            plant.getPosition().y+=downVelocity.y*dt;
        } else if (stateTime >= config.jumpDownDuration && jumpUpOver){
            target.takeDamage(config.baseDamage, false, true);
            plant.dispose();
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath,currentClip,stateTime,plant.getPosition(),scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
