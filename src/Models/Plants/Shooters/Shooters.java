package Models.Plants.Shooters;

import Models.DataTypes.Vector2;
import Models.Plants.Plant;
import Models.Plants.PlantCategory;
import Models.Plants.PlantTag;
import Models.Plants.PlantType;

import java.util.List;

public class Shooters extends Plant {
    protected PlantType type;
    protected BulletType defaultBulletType;
    protected int bulletsInWave;
    protected int delayBetweenWaves;

    public Shooters(Vector2 position, PlantType type, PlantCategory category, List<PlantTag> tags, int baseHealth, int sunCost, int cooldown) {
        super(position, type, category, tags, baseHealth, sunCost, cooldown);
    }
}
