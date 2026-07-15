package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.GameContext;

/**
 * A discrete, re-usable plant behaviour plug-in — the Strategy-pattern
 * counterpart of {@link pvz.Models.Entities.Zombies.skills.ZombieSkill}.
 *
 * <p>Attached to a {@link Plant} instance by {@link pvz.Models.Entities.Plants.PlantFactory}.
 * On every tick, {@link pvz.Models.Entities.Plants.fsm.PlantIdleState} asks the
 * action whether it is ready; if so, the plant transitions to
 * {@link pvz.Models.Entities.Plants.fsm.PlantActionState}, which fires it.
 */
public interface PlantAction {

    /** Evaluated every tick while the plant is idle. Should be cheap. */
    boolean shouldTrigger(Plant plant, GameContext ctx);

    /** Performs the action's game-world effect. Called once per activation. */
    void execute(Plant plant, GameContext ctx);

    /** Short label for CLI debug output, e.g. {@code "Shoot"}, {@code "ProduceSun"}. */
    String getName();
}
