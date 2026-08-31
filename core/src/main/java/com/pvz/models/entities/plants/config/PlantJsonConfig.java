package com.pvz.models.entities.plants.config;


public class PlantJsonConfig {

    public int id;
    public String name;
    public String type;
    public String category;
    public String tags;
    public int sunCost;
    public float baseHp;
    public Float actionIntervalSeconds;
    public Float rechargeSeconds;
    public PlantActionConfig attackConfig;
    public PlantActionConfig feedConfig;
    public PamAnimationConfig pamAnimationConfig;
    public String description;
    public String onPlantFoodDescription;
    public String overallDescription;
    public String funDescription;
    public java.util.List<LevelUpgradeConfig> levelUpgrades;
}
