package pvz.Models.Entities.Zombies.fsm;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;

/**
 * Terminal FSM state — the zombie is dead.
 *
 * <p>The engine detects a dead zombie via {@link Zombie#isDead()} and
 * removes it from the game on the next tick cycle. This state exists
 * solely so the {@code zombies info} CLI command can show "Dead" for
 * zombies that died mid-tick before the engine processes removals.
 */
public class DeadState implements ZombieState {

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        // Death logging is handled inside Zombie.onDeath() to ensure it fires
        // exactly once regardless of how death was triggered.
    }

    @Override
    public ZombieState tick(Zombie zombie, GameContext ctx) {
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
}
