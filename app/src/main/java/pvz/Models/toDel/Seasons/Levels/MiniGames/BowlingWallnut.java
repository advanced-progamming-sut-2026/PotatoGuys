package pvz.Models.toDel.Seasons.Levels.MiniGames;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.GameContext;
import pvz.Models.Entities.Plants.actions.PlantAction;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;

public class BowlingWallnut extends Plant{
    public BowlingWallnut(PlantPropertySheet sheet, PlantAction action, int col, int lane, int level, boolean isBoosted, GameContext context) {
        super(sheet, action, col, lane, level, isBoosted, context);
    }

    public void enter(){}
    public void update(){}

    @Override
    public void dispose() {

    }
}
