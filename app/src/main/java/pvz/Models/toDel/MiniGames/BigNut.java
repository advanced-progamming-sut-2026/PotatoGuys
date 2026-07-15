package pvz.Models.toDel.MiniGames;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.actions.PlantAction;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Games.GameContext;

public class BigNut extends Plant{
    public BigNut(PlantPropertySheet sheet, PlantAction action, int col, int lane, int level, boolean isBoosted, GameContext context) {
        super(sheet, action, col, lane, level, isBoosted, context);
    }

    @Override
    public void enter(){}
    @Override
    public void update()
    {super.update();}

    @Override
    public void dispose() {

    }
}
