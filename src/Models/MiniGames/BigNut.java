package Models.MiniGames;

import java.util.List;

import Models.DataTypes.Vector2;
import Models.Plants.Plant;
import Models.Plants.Strategy;
import Models.Plants.Enums.PlantCategory;
import Models.Plants.Enums.PlantTag;
import Models.Plants.Enums.PlantType;

public class BigNut extends Plant{
    public BigNut(Vector2 position, Vector2 speed, PlantType type, PlantCategory category, List<PlantTag> tags, Strategy strategy, int sunCost, int baseHp, int baseRecharge, int baseActionInterval, int recharge, int actionInterval, int level, boolean isBoosted) {
        super(position, speed, type, category, tags, strategy, sunCost, baseHp, baseRecharge, baseActionInterval, recharge, actionInterval, level, isBoosted);
    }

    @Override
    public void onFirstTick(){}
    @Override
    public void onTick()
    {super.onTick();}

    @Override
    public void dispose() {

    }
}
