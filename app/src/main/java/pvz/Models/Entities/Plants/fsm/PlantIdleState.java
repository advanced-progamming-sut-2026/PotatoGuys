package pvz.Models.Entities.Plants.fsm;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.GameContext;


/**
 * Default resting state — the plant waits for its {@link
 * pvz.Models.Entities.Plants.actions.PlantAction} to become ready, mirroring
 * {@link pvz.Models.Entities.Zombies.fsm.WalkState}'s per-tick skill check.
 */
public class PlantIdleState implements PlantState {

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        // no visual cue needed for idling
    }

    @Override
    public PlantState tick(Plant plant, GameContext ctx) {
        if (plant.getAction().shouldTrigger(plant, ctx)) {
            return new PlantActionState(plant.getAction());
        }
        return this;
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Idle";
    }
}
