package pvz.Models.MiniGames;

import java.util.List;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.Strategy;
import pvz.Models.Entities.Plants.Enums.PlantCategory;
import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Plants.Enums.PlantType;

public class BowlingWallnut extends Plant{
    public BowlingWallnut(Vector2 position, Vector2 speed, PlantType type, PlantCategory category, List<PlantTag> tags, Strategy strategy, int sunCost, int baseHp, int baseRecharge, int baseActionInterval, int recharge, int actionInterval, int level, boolean isBoosted) {
        super(position, speed, type, category, tags, strategy, sunCost, baseHp, baseRecharge, baseActionInterval, recharge, actionInterval, level, isBoosted);
    }

    public void enter(){}
    public void update(){}

    @Override
    public void dispose() {

    }
}
