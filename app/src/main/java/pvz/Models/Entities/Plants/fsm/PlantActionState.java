package pvz.Models.Entities.Plants.fsm;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.actions.PlantAction;
import pvz.Models.Games.GameContext;

/**
 * Generic action-execution state — the plant counterpart of
 * {@link pvz.Models.Entities.Zombies.fsm.SpecialActionState}.
 *
 * <p>Fires the action once on {@link #onEnter}, pauses for {@link #pauseTicksRemaining}
 * ticks (0 = instantaneous), then returns to {@link PlantIdleState} — unless
 * the plant killed itself during the action (single-use Explosives/Mints),
 * in which case the FSM simply stops mattering (the engine removes it).
 */
public class PlantActionState implements PlantState {

    private final PlantAction action;
    private int pauseTicksRemaining;

    public PlantActionState(PlantAction action, int pauseTicks) {
        this.action = action;
        this.pauseTicksRemaining = pauseTicks;
    }

    public PlantActionState(PlantAction action) {
        this(action, 0);
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        action.execute(plant, ctx);
    }

    @Override
    public PlantState tick(Plant plant, GameContext ctx) {
        if (plant.isDead()) return this;
        if (pauseTicksRemaining > 0) {
            pauseTicksRemaining--;
            return this;
        }
        return new PlantIdleState();
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Action[" + action.getName() + "]";
    }
}
