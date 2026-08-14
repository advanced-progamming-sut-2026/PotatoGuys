package com.pvz.models.entities.zombies.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.config.ZombieSkillConfig;
import com.pvz.models.entities.zombies.data.ZombieRegistry;
import com.pvz.models.games.GameContext;

import java.util.Map;

public class FrozenState extends ZombieState{
    float lastStateFrameTime;
    ZombieState lastState;
    float duration;
    String pamPath;
    String clip;
    Map<String, Boolean> partVisibility;

    protected FrozenState(ZombieState lastState, float lastStateFrameTime, float duration,
                          String pamPath, String clip, Map<String, Boolean> partVisibility) {
        super(null);
        this.lastState=lastState;
        this.lastStateFrameTime=lastStateFrameTime;
        this.duration=duration;
        this.pamPath=pamPath;
        this.clip=clip;
        this.partVisibility=partVisibility;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        super.onEnter(zombie, ctx);
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        super.update(zombie, ctx, dt);
        if (stateTime>=duration){
            return lastState;
        }
        return this;
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        return new FrameConfig(pamPath,clip,lastStateFrameTime,zombie.getPosition(),
            new Vector2(0.65f,0.65f),partVisibility,false);
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        super.onExit(zombie, ctx);
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        return false;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {

    }

    @Override
    public String getName() {
        return "";
    }
}
