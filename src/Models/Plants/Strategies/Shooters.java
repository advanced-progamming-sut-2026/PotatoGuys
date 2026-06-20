package Models.Plants.Strategies;

import Models.Plants.Strategy;
import Models.Plants.Enums.PlantType;

public class Shooters implements Strategy {
    private PlantType type;
    private BulletType defaultBulletType;
    private int bulletsInWave;
    private int delayBetweenWaves;

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

    public PlantType getType() {
        return type;
    }

    public BulletType getDefaultBulletType() {
        return defaultBulletType;
    }

    public int getBulletsInWave() {
        return bulletsInWave;
    }

    public int getDelayBetweenWaves() {
        return delayBetweenWaves;
    }
}
