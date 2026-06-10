package Models.Plants.Strategies;

import Models.DataTypes.Vector2;
import Models.Plants.Plant;
import Models.Plants.Strategy;
import Models.Plants.Enums.PlantCategory;
import Models.Plants.Enums.PlantTag;
import Models.Plants.Enums.PlantType;
import Models.Plants.Shooters;

import java.util.List;

public class Shooters implements Strategy {
    protected PlantType type;
    protected BulletType defaultBulletType;
    protected int bulletsInWave;
    protected int delayBetweenWaves;

    public Shooters(PlantType type , BulletType defaultBulletType, int bulletsInWave,
            int delayBetweenWaves) {
        this.type = type;
        this.defaultBulletType = defaultBulletType;
        this.bulletsInWave = bulletsInWave;
        this.delayBetweenWaves = delayBetweenWaves;
    }
    public void defaultApply(){

    }
    public void boostApply(){

    }
}
