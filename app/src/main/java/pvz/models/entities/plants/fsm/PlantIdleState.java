package pvz.models.entities.plants.fsm;

import pvz.models.entities.plants.Plant;
import pvz.models.games.GameContext;


/**
 * Default resting state — the plant waits for its {@link
 * pvz.models.entities.plants.actions.PlantAction} to become ready, mirroring
 * {@link pvz.models.entities.zombies.fsm.WalkState}'s per-tick skill check.
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
