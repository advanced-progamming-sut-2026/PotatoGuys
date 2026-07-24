package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.games.GameContext;

public interface PlantAction {

    boolean shouldTrigger(Plant plant, GameContext ctx);

    void execute(Plant plant, GameContext ctx);

    String getName();
}
