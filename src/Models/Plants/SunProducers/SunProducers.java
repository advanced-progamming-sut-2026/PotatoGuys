package Models.Plants.SunProducers;

import Models.DataTypes.Vector2;
import Models.Plants.Plant;
import Models.Plants.PlantCategory;
import Models.Plants.PlantTag;
import Models.Plants.PlantType;

import java.util.List;

public class SunProducers extends Plant {
    private SunType defaultSunType;
    private int sunProduceTime;

    public SunProducers(Vector2 position, PlantType type, PlantCategory category, List<PlantTag> tags, int baseHealth, int sunCost, int cooldown) {
        super(position, type, category, tags, baseHealth, sunCost, cooldown);
    }
}
