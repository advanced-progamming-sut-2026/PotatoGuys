package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieActionConfig;
import com.pvz.models.entities.zombies.config.ZombieSkillConfig;
import com.pvz.models.games.GameContext;

/**
 * Terminal FSM state — the zombie is dead.
 *
 * <p>The engine detects a dead zombie via {@link Zombie#isDead()} and
 * removes it from the game on the next tick cycle. This state exists
 * solely so the {@code zombies info} CLI command can show "Dead" for
 * zombies that died mid-tick before the engine processes removals.
 */
public class DeadState extends ZombieState {

    public DeadState() {
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
        return "";
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        // Death logging is handled inside Zombie.onDeath() to ensure it fires
        // exactly once regardless of how death was triggered.
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        return this; // stay in dead state; engine will deregister this zombie
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // nothing — dead zombies don't exit this state
    }

    @Override
    public String getLabel() {
        return "Dead";
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        ZombieActionConfig die = zombie.getSheet().dieConfig;
        return zombie.drawClip(die != null ? die.label : "die");
    }
}
