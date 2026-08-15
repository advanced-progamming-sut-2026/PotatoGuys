package com.pvz.models.entities.plants.actions.explosive;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.Effect;
import com.pvz.models.entities.effects.Explosion;
import com.pvz.models.entities.effects.JalapenoFire;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.ExplosiveConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

import java.util.List;

public class ExplosionAction extends PlantAction {
    ExplosiveConfig config;
    Zombie target;
    List<Tile> targetTiles;
    boolean explosionDone;
    boolean dealDamageDone;
    public ExplosionAction(ExplosiveConfig config, Zombie target, List<Tile> targetTiles){
        this.config=config;
        this.target=target;
        this.targetTiles=targetTiles;
        explosionDone=false;
        dealDamageDone=false;
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
        if (!explosionDone && stateTime>=config.explosionTime){
            explosionDone=true;
            if (plant.getType()== PlantType.Jalapeno){
                for (int i = 0; i < ctx.getMap().getColumns(); i++) {
                    Vector2 pos=new Vector2(GameController.colToWorldX(i),GameController.laneToWorldY(plant.getLane()));
                    ctx.addEffect(new JalapenoFire(ctx,pos));
                }
            } else if (plant.getType()!=PlantType.Doomshroom && plant.getType()!=PlantType.IcebergLettuce){
                ctx.addEffect(new Explosion(ctx, plant.getPosition(), config.explosionType, config.explosionIntensity));
            }
        }
        if (!dealDamageDone && stateTime>=config.damageDealTime){
            dealDamageDone=true;
            if (target!=null) {
                target.takeDamage(config.baseDamage);
                if (plant.getType()==PlantType.IcebergLettuce){
                    target.setFrozen(config.effectDuration);
                }
            }
            if (targetTiles!=null && !targetTiles.isEmpty()){
                for (Tile t: targetTiles){
                    for (Zombie z: ctx.getZombiesAt(t.getCol(),t.getLane())){
                        z.takeDamage(config.baseDamage);
                    }
                    t.processHit(config.baseDamage);
                }
            }
        }
        if (explosionDone && dealDamageDone) plant.dispose();
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath, config.explosionClip,stateTime,position,scale,null,false);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
