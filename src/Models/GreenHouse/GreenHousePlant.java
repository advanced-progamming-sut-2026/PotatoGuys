package Models.GreenHouse;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class GreenHousePlant {

    protected LocalDateTime plantedTime;
    protected int growthHours;

    public GreenHousePlant(int growthHours) {
        this.growthHours = growthHours;
        this.plantedTime = LocalDateTime.now();
    }

    public boolean isReady() {
        LocalDateTime finish = plantedTime.plusHours(growthHours);
        return !LocalDateTime.now().isBefore(finish);
    }

    public int remainingHours() {
        LocalDateTime finish = plantedTime.plusHours(growthHours);
        Duration duration = Duration.between(LocalDateTime.now(), finish);

        if (duration.isNegative() || duration.isZero()) {
            return 0;
        }

        return (int) Math.ceil(duration.getSeconds() / 3600.0);
    }

    public void makeReadyNow() {
        plantedTime = LocalDateTime.now().minusHours(growthHours);
    }

    public LocalDateTime getPlantedTime() {
        return plantedTime;
    }

    public int getGrowthHours() {
        return growthHours;
    }
}
