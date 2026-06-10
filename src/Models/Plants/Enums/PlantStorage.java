package Models.Plants.Enums;

import java.util.List;

import Models.Plants.Strategy;
import Models.Plants.Strategies.SunProducers;
import Models.Plants.SunProducers.SunType;

public enum PlantStorage {

    Sunflower(PlantType.Sunflower , PlantCategory.SUN_PRODUCER , PlantTag.DAY , 50 , 300 , 0 , 24 , 5 , new SunProducers())

    
    private PlantType type;
    private PlantCategory category;
    private List<PlantTag> tags;
    private int sunCost;
    private int baseHP;
    private int damage;
    private int baseActionInterval;
    private int baseRecharge;
    private Strategy strategy;

    private PlantStorage(PlantType type, PlantCategory category, List<PlantTag> tags, int sunCost, int baseHP,
            int baseRecharge, int baseActionInterval, Strategy strategy) {
        this.type = type;
        this.category = category;
        this.tags = tags;
        this.sunCost = sunCost;
        this.baseHP = baseHP;
        this.baseRecharge = baseRecharge;
        this.baseActionInterval = baseActionInterval;
        this.strategy = strategy;
    }
}
