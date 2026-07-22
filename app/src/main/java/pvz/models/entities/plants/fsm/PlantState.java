package pvz.models.entities.plants.fsm;

import pvz.models.entities.plants.Plant;
import pvz.models.games.GameContext;


/**
 * One node in the plant's Finite State Machine — the structural mirror of
 * {@link pvz.models.entities.zombies.fsm.ZombieState}.
 *
 * <p><b>Contract</b>:
 * <ol>
 *   <li>{@link #onEnter} is called exactly once when the plant transitions into this state.</li>
 *   <li>{@link #tick} is called every game tick while this state is current.
 *       It must return {@code this} to stay in the same state, or a <em>new</em>
 *       state instance to trigger a transition.</li>
 *   <li>{@link #onExit} is called exactly once when the plant leaves this state.</li>
 * </ol>
 */
public interface PlantState {

    /** Side-effects when entering this state (e.g. logging, animation cue). */
    void onEnter(Plant plant, GameContext ctx);

    /**
     * Core update logic.
     *
     * @return the next state ({@code this} = no transition, any other object = transition)
     */
    PlantState tick(Plant plant, GameContext ctx);

    /** Side-effects when leaving this state. */
    void onExit(Plant plant, GameContext ctx);

    /** Short human-readable label used by {@code plants info} CLI command. */
    String getLabel();
}
