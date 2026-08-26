package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieActionConfig;
import com.pvz.models.games.GameContext;

/**
 * Lightweight visual-only state for the network guest side: plays the eat
 * animation without dealing any damage or tracking a plant target. The host
 * drives all gameplay; this state exists solely so the guest renders the
 * correct animation when the snapshot says the zombie is eating.
 */
public class VisualEatState extends ZombieState {

    public VisualEatState() {
        super(null);
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
        return "VisualEat";
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        return this;
    }

    @Override
    public String getLabel() {
        return "VisualEat";
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        ZombieActionConfig eat = zombie.getSheet().eatConfig;
        return zombie.drawClip(eat != null ? eat.label : "eat");
    }
}
