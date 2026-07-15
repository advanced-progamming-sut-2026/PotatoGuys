package pvz.Models.MiniGames;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;
import pvz.Models.Entities.Plants.actions.PlantAction;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;

public class ExplodeONut extends Plant{
    public ExplodeONut(PlantPropertySheet sheet, PlantAction action, int col, int lane, int level, boolean isBoosted, PlantContext context) {
        super(sheet, action, col, lane, level, isBoosted, context);
    }

    public void enter(){}
    public void update(){}

    @Override
    public void dispose() {

    }
}
