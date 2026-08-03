package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * One node in the zombie's Finite State Machine.
 *
 * <p><b>Contract</b>:
 * <ol>
 *   <li>{@link #onEnter} is called exactly once when the zombie transitions into this state.</li>
 *   <li>{@link #tick} is called every game tick while this state is current.
 *       It must return {@code this} to stay in the same state, or a <em>new</em>
 *       state instance to trigger a transition.</li>
 *   <li>{@link #onExit} is called exactly once when the zombie leaves this state.</li>
 * </ol>
 *
 * <p>States are designed to be lightweight objects created on each transition,
 * so they may carry per-transition data (e.g. the target plant cell in {@code EatState}).
 */
public interface ZombieState {

    /** Side-effects when entering this state (e.g. logging, animation cue). */
    void onEnter(Zombie zombie, GameContext ctx);

    /**
     * Core update logic.
     *
     * @return the next state ({@code this} = no transition, any other object = transition)
     */
    ZombieState tick(Zombie zombie, GameContext ctx);

    /** Side-effects when leaving this state. */
    void onExit(Zombie zombie, GameContext ctx);

    /** Short human-readable label used by {@code zombies info} CLI command. */
    String getLabel();
}
