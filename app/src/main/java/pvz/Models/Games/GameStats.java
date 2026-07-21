package pvz.models.games;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pvz.models.entities.plants.enums.PlantType;

public class GameStats {
    private int sunCollected;
    private int zombiesKilled;
    private int zombiesKilledByPlant;
    private int zombiesKilledByCactus;
    private int plantsLost;
    private int explosivePlantsUsed;
    private int lawnmowerKills;
    private boolean firstWaveStarted;
    private int tickOfFirstWave;
    private int zombiesKilledAfterFirstWaveIn30Sec;
    private Set<Integer> columnsUsedForPlanting;
    private Set<Integer> rowsUsedForPlanting;
    private boolean usedSunProducers;
    private int sunProducerCount;
    private Set<PlantType> plantsUsedToKill;
    private Set<String> plantFamiliesUsed;
    private boolean usesNightPlants;
    private int zombiesKilledInCol0NoMower;
    private String currentSeasonName;
    private int zombiesKilledBySeason;
    private Map<String, Integer> zombiesKilledBySeasonMap;
    private int consecutiveWinsOnHighDiff;
    private int lastPlantsLostCount;
    private Map<String, Integer> zombiesKilledByPlantType;
    private Set<String> killingFamiliesUsed;

    public GameStats() {
        this.columnsUsedForPlanting = new HashSet<>();
        this.rowsUsedForPlanting = new HashSet<>();
        this.plantsUsedToKill = new HashSet<>();
        this.plantFamiliesUsed = new HashSet<>();
        this.zombiesKilledBySeasonMap = new HashMap<>();
        this.zombiesKilledByPlantType = new HashMap<>();
        this.killingFamiliesUsed = new HashSet<>();
    }

    public void reset() {
        sunCollected = 0;
        zombiesKilled = 0;
        zombiesKilledByPlant = 0;
        zombiesKilledByCactus = 0;
        plantsLost = 0;
        explosivePlantsUsed = 0;
        lawnmowerKills = 0;
        firstWaveStarted = false;
        tickOfFirstWave = 0;
        zombiesKilledAfterFirstWaveIn30Sec = 0;
        columnsUsedForPlanting.clear();
        rowsUsedForPlanting.clear();
        usedSunProducers = false;
        sunProducerCount = 0;
        plantsUsedToKill.clear();
        plantFamiliesUsed.clear();
        usesNightPlants = false;
        zombiesKilledInCol0NoMower = 0;
        zombiesKilledBySeason = 0;
        lastPlantsLostCount = 0;
        zombiesKilledByPlantType.clear();
        killingFamiliesUsed.clear();
    }

    public void onSunCollected(int amount) { sunCollected += amount; }
    public void onZombieKilled() { zombiesKilled++; }
    public void onZombieKilledByPlant(PlantType type) {
        zombiesKilledByPlant++;
        plantsUsedToKill.add(type);
        zombiesKilledByPlantType.merge(type.name(), 1, Integer::sum);
        String family = getPlantFamily(type);
        if (family != null) killingFamiliesUsed.add(family);
        if (type == PlantType.Cactus) zombiesKilledByCactus++;
    }
    public void onPlantLost() { plantsLost++; }
    public void onExplosivePlantUsed() { explosivePlantsUsed++; }
    public void onLawnmowerKill(int count) { lawnmowerKills += count; }
    public void onFirstWaveStart(int tick) {
        if (!firstWaveStarted) {
            firstWaveStarted = true;
            tickOfFirstWave = tick;
        }
    }
    public void onPlantPlaced(int col, int row) {
        columnsUsedForPlanting.add(col);
        rowsUsedForPlanting.add(row);
    }
    public void onSunProducerPlaced() { sunProducerCount++; usedSunProducers = true; }
    public void onPlantUsedToKill(PlantType type) { plantsUsedToKill.add(type); }
    public void onFamilyUsed(String family) { plantFamiliesUsed.add(family); }
    public void onNightPlantUsed() { usesNightPlants = true; }
    public void onZombieKilledInCol0(boolean hasMower) {
        if (!hasMower) zombiesKilledInCol0NoMower++;
    }
    public void onZombieKilledInSeason(String season) {
        zombiesKilledBySeason++;
        zombiesKilledBySeasonMap.merge(season, 1, Integer::sum);
    }
    public void onWaveProgress(int currentTick) {
        if (firstWaveStarted && (currentTick - tickOfFirstWave) <= 60) {
            zombiesKilledAfterFirstWaveIn30Sec = zombiesKilled;
        }
    }

    public int getSunCollected() { return sunCollected; }
    public int getZombiesKilled() { return zombiesKilled; }
    public int getZombiesKilledByPlant() { return zombiesKilledByPlant; }
    public int getZombiesKilledByCactus() { return zombiesKilledByCactus; }
    public int getPlantsLost() { return plantsLost; }
    public int getExplosivePlantsUsed() { return explosivePlantsUsed; }
    public int getLawnmowerKills() { return lawnmowerKills; }
    public boolean isFirstWaveStarted() { return firstWaveStarted; }
    public int getTickOfFirstWave() { return tickOfFirstWave; }
    public int getZombiesKilledAfterFirstWaveIn30Sec() { return zombiesKilledAfterFirstWaveIn30Sec; }
    public Set<Integer> getColumnsUsedForPlanting() { return columnsUsedForPlanting; }
    public Set<Integer> getRowsUsedForPlanting() { return rowsUsedForPlanting; }
    public boolean isUsedSunProducers() { return usedSunProducers; }
    public int getSunProducerCount() { return sunProducerCount; }
    public Set<PlantType> getPlantsUsedToKill() { return plantsUsedToKill; }
    public Set<String> getPlantFamiliesUsed() { return plantFamiliesUsed; }
    public boolean isUsesNightPlants() { return usesNightPlants; }
    public int getZombiesKilledInCol0NoMower() { return zombiesKilledInCol0NoMower; }
    public String getCurrentSeasonName() { return currentSeasonName; }
    public int getZombiesKilledBySeason() { return zombiesKilledBySeason; }
    public Map<String, Integer> getZombiesKilledBySeasonMap() { return zombiesKilledBySeasonMap; }
    public int getConsecutiveWinsOnHighDiff() { return consecutiveWinsOnHighDiff; }
    public int getLastPlantsLostCount() { return lastPlantsLostCount; }

    public void setCurrentSeasonName(String name) { this.currentSeasonName = name; }
    public void setConsecutiveWinsOnHighDiff(int val) { this.consecutiveWinsOnHighDiff = val; }
    public void setLastPlantsLostCount(int val) { this.lastPlantsLostCount = val; }

    public int getZombiesKilledByPlantType(String plantName) {
        return zombiesKilledByPlantType.getOrDefault(plantName, 0);
    }
    public Set<String> getKillingFamiliesUsed() { return killingFamiliesUsed; }

    public static String getPlantFamily(PlantType type) {
        switch (type) {
            case Peashooter: case Repeater: case Threepeater: case SnowPea:
            case Rotobaga: case PeaPod: case SplitPea: case Citron:
            case Caulipower: case ElectricBlueberry: case BowlingBulb:
            case Cactus: case FirePeashooter: case Starfruit: case GooPeashooter:
            case MegaGatlingPea:
                return "SHOOTER";
            case Sunflower: case TwinSunflower: case Sunshroom: case PrimalSunflower:
            case GoldBloom:
                return "SUN_PRODUCER";
            case Cabbagepult: case Kernelpult: case Melonpult: case WinterMelon:
            case Pepperpult:
                return "LOBBER";
            case CherryBomb: case Jalapeno: case Doomshroom: case Grapeshot:
            case PotatoMine: case PrimalPotatoMine:
                return "EXPLOSIVE";
            case BonkChoy: case Chomper: case WasabiWhip: case Kiwibeast:
                return "MELEE";
            case Wallnut: case Tallnut: case Endurian: case Garlic:
            case SweetPotato: case Pumpkin:
                return "WALL_NUT";
            case Seashroom: case Puffshroom: case Fumeshroom: case Magnetshroom:
            case Hypnoshroom: case Iceshroom: case Cattail: case TangleKelp:
            case IcebergLettuce: case Torchwood: case SunBean: case Explodeonut:
            case GraveBuster: case HotPotato: case LilyPad: case Imitater:
                return "NIGHT_PLANT";
            default:
                return null;
        }
    }
}
