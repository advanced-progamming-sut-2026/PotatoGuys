package pvz.Models.Entities.Plants.fsm;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;

/**
 * One node in the plant's Finite State Machine — the structural mirror of
 * {@link pvz.Models.Entities.Zombies.fsm.ZombieState}.
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
    void onEnter(Plant plant, PlantContext ctx);

    /**
     * Core update logic.
     *
     * @return the next state ({@code this} = no transition, any other object = transition)
     */
    PlantState tick(Plant plant, PlantContext ctx);

    /** Side-effects when leaving this state. */
    void onExit(Plant plant, PlantContext ctx);

    /** Short human-readable label used by {@code plants info} CLI command. */
    String getLabel();
}
