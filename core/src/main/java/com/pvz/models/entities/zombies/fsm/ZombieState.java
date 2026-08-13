package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * One node in the zombie's Finite State Machine.
 *
 * <p><b>Contract</b>:
 * <ol>
 *   <li>{@link #onEnter} is called exactly once when the zombie transitions into this state.</li>
 *   <li>{@link #update} is called every game tick while this state is current.
 *       It must return {@code this} to stay in the same state, or a <em>new</em>
 *       state instance to trigger a transition.</li>
 *   <li>{@link #onExit} is called exactly once when the zombie leaves this state.</li>
 * </ol>
 *
 * <p>States are designed to be lightweight objects created on each transition,
 * so they may carry per-transition data (e.g. the target plant cell in {@code EatState}).
 * Stateful action states (see {@link
 * com.pvz.models.entities.zombies.skills.ZombieSkill}) live on the zombie instead
 * and reuse their instance across transitions.
 */
public abstract class ZombieState {

    /** Seconds elapsed since this state became current. */
    protected float stateTime = 0f;

    /** Side-effects when entering this state (e.g. logging, animation cue). */
    public abstract void onEnter(Zombie zombie, GameContext ctx);

    /**
     * Core update logic.
     *
     * @return the next state ({@code this} = no transition, any other object = transition)
     */
    public abstract ZombieState update(Zombie zombie, GameContext ctx, float dt);

    /** Side-effects when leaving this state. */
    public abstract void onExit(Zombie zombie, GameContext ctx);

    /** Short human-readable label used by {@code zombies info} CLI command. */
    public abstract String getLabel();

    /**
     * Produces the PAM frame this state renders — clip label comes from the
     * zombie's config (walk/eat/die/skill), falling back to a default when the
     * config is missing. Returns {@code null} to render nothing.
     */
    public abstract FrameConfig draw(Zombie zombie, GameContext ctx);
}
