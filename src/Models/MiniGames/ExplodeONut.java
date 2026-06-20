package Models.MiniGames;

import Models.DataTypes.Vector2;
import Models.Plants.Enums.PlantCategory;
import Models.Plants.Enums.PlantTag;
import Models.Plants.Enums.PlantType;
import Models.Plants.Plant;
import Models.Plants.Strategy;

import java.util.List;

public class ExplodeONut extends Plant{
    public ExplodeONut(Vector2 position, Vector2 speed, PlantType type, PlantCategory category, List<PlantTag> tags, Strategy strategy, int sunCost, int baseHp, int baseRecharge, int baseActionInterval, int recharge, int actionInterval, int level, boolean isBoosted) {
        super(position, speed, type, category, tags, strategy, sunCost, baseHp, baseRecharge, baseActionInterval, recharge, actionInterval, level, isBoosted);
    }

    public void onFirstTick(){}
    public void onTick(){}

    @Override
    public void dispose() {

    }
}
