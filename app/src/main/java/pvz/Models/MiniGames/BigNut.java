package pvz.Models.MiniGames;

import java.util.List;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.Strategy;
import pvz.Models.Entities.Plants.Enums.PlantCategory;
import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Plants.Enums.PlantType;

public class BigNut extends Plant{
    public BigNut(Vector2 position, Vector2 speed, PlantType type, PlantCategory category, List<PlantTag> tags, Strategy strategy, int sunCost, int baseHp, int baseRecharge, int baseActionInterval, int recharge, int actionInterval, int level, boolean isBoosted) {
        super(position, type, category, tags, strategy, sunCost, baseHp, baseRecharge, baseActionInterval, level, isBoosted);
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
