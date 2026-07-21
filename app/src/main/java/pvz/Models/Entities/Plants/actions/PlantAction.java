package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.games.GameContext;

/**
 * A discrete, re-usable plant behaviour plug-in — the Strategy-pattern
 * counterpart of {@link pvz.models.entities.zombies.skills.ZombieSkill}.
 *
 * <p>Attached to a {@link Plant} instance by {@link pvz.models.entities.plants.PlantFactory}.
 * On every tick, {@link pvz.models.entities.plants.fsm.PlantIdleState} asks the
 * action whether it is ready; if so, the plant transitions to
 * {@link pvz.models.entities.plants.fsm.PlantActionState}, which fires it.
 */
public interface PlantAction {

    /** Evaluated every tick while the plant is idle. Should be cheap. */
    boolean shouldTrigger(Plant plant, GameContext ctx);

    /** Performs the action's game-world effect. Called once per activation. */
    void execute(Plant plant, GameContext ctx);

    /** Short label for CLI debug output, e.g. {@code "Shoot"}, {@code "ProduceSun"}. */
    String getName();
}
