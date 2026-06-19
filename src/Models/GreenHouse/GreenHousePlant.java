package Models.GreenHouse;

import java.time.LocalDateTime;

public abstract class GreenHousePlant {

    private LocalDateTime plantedTime;
    private int growthHours;
    private LocalDateTime harvestTime;

    public GreenHousePlant(int growthHours) {
        this.growthHours = growthHours;
        this.plantedTime = LocalDateTime.now();
    }

    public boolean isReady() {
        LocalDateTime finish = getPlantedTime().plusHours(getGrowthHours());
        return LocalDateTime.now().isAfter(finish);
    }

    public long remainingHours() {
        LocalDateTime finish = getPlantedTime().plusHours(getGrowthHours());
        return java.time.Duration.between(LocalDateTime.now(), finish).toHours();
    }

    public LocalDateTime getPlantedTime() {
        return plantedTime;
    }

    public int getGrowthHours() {
        return growthHours;
    }

    public LocalDateTime getHarvestTime() {
        return harvestTime;
    }
}
