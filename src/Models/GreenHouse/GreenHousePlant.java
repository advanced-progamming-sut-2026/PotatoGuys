package Models.GreenHouse;

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
        return LocalDateTime.now().isAfter(finish);
    }

    public long remainingHours() {
        LocalDateTime finish = plantedTime.plusHours(growthHours);
        return java.time.Duration.between(LocalDateTime.now(), finish).toHours();
    }
}
