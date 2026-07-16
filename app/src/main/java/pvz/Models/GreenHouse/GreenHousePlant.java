package pvz.Models.GreenHouse;

import java.time.LocalDateTime;

public class GreenHousePlant {

    public static final int MARIGOLD_GROWTH_HOURS = 2;
    public static final int UNLOCKED_GROWTH_HOURS = 8;
    public static final int MARIGOLD_REWARD = 500;

    private String kind;          // "mariGold" or "unlocked"
    private String plantType;     // null for mariGold
    private String plantedTime;   // ISO LocalDateTime string
    private int growthHours;

    public GreenHousePlant() {
    }

    private GreenHousePlant(String kind, String plantType, int growthHours) {
        this.kind = kind;
        this.plantType = plantType;
        this.growthHours = growthHours;
        this.plantedTime = LocalDateTime.now().toString();
    }

    public static GreenHousePlant createMariGold() {
        return new GreenHousePlant("mariGold", null, MARIGOLD_GROWTH_HOURS);
    }

    public static GreenHousePlant createUnlocked(String plantType) {
        return new GreenHousePlant("unlocked", plantType, UNLOCKED_GROWTH_HOURS);
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getPlantType() {
        return plantType;
    }

    public void setPlantType(String plantType) {
        this.plantType = plantType;
    }

    public String getPlantedTime() {
        return plantedTime;
    }

    public void setPlantedTime(String plantedTime) {
        this.plantedTime = plantedTime;
    }

    public int getGrowthHours() {
        return growthHours;
    }

    public void setGrowthHours(int growthHours) {
        this.growthHours = growthHours;
    }

    public boolean isMariGold() {
        return "mariGold".equals(kind);
    }

    public boolean isReady() {
        LocalDateTime finish = LocalDateTime.parse(plantedTime).plusHours(growthHours);
        return !LocalDateTime.now().isBefore(finish);
    }

    public int remainingHours() {
        LocalDateTime finish = LocalDateTime.parse(plantedTime).plusHours(growthHours);
        java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), finish);
        if (duration.isNegative() || duration.isZero()) {
            return 0;
        }
        return (int) Math.ceil(duration.getSeconds() / 3600.0);
    }

    public void makeReadyNow() {
        plantedTime = LocalDateTime.now().minusHours(growthHours).toString();
    }
}
