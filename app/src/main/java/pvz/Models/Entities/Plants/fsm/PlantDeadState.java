package pvz.models.entities.plants.fsm;

import pvz.models.entities.plants.Plant;
import pvz.models.games.GameContext;


/**
 * Terminal FSM state — the plant is destroyed. Mirrors
 * {@link pvz.models.entities.zombies.fsm.DeadState}; present mainly so a
 * {@code plants info} CLI command can display "Dead" for a plant that died
 * mid-tick before the engine processes removals.
 */
public class PlantDeadState implements PlantState {

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        // death logging happens inside Plant.kill() so it fires exactly once
    }

    @Override
    public PlantState tick(Plant plant, GameContext ctx) {
        return this;
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Dead";
    }
}
