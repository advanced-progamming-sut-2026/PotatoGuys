package pvz.Models.Entities.Plants.fsm;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;

/**
 * Terminal FSM state — the plant is destroyed. Mirrors
 * {@link pvz.Models.Entities.Zombies.fsm.DeadState}; present mainly so a
 * {@code plants info} CLI command can display "Dead" for a plant that died
 * mid-tick before the engine processes removals.
 */
public class PlantDeadState implements PlantState {

    @Override
    public void onEnter(Plant plant, PlantContext ctx) {
        // death logging happens inside Plant.kill() so it fires exactly once
    }

    @Override
    public PlantState tick(Plant plant, PlantContext ctx) {
        return this;
    }

    @Override
    public void onExit(Plant plant, PlantContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Dead";
    }
}
