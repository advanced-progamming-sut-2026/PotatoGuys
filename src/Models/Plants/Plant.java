package Models.Plants;

import java.util.List;

import Models.DataTypes.Vector2;

public abstract class Plant {
    private Vector2 position;
    private Vector2 speed;

    private final PlantType type;
    private final PlantCategory category;
    private final List<PlantTag> tags;
    private final int baseHealth;
    private final int sunCost;
    private final int cooldown;

    private int health;
    private int level;
    private boolean isBoosted;

    public Plant(Vector2 position, PlantType type, PlantCategory category, List<PlantTag> tags, int baseHealth
            , int sunCost, int cooldown) {
        this.position = position;
        this.type = type;
        this.category = category;
        this.tags = tags;
        this.baseHealth = baseHealth;
        this.health = baseHealth;
        this.sunCost = sunCost;
        this.cooldown = cooldown;
        this.level=0;
        this.isBoosted=false;
    }

    public void takeDamage(int amount) {

    }

    public PlantType gettype(){
        return type;
    }

    public Vector2 getPosition() {
        return position;
    }

    public PlantCategory getCategory() {
        return category;
    }

    public List<PlantTag> getTags() {
        return tags;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public int getSunCost() {
        return sunCost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getHealth() {
        return health;
    }

    public int getLevel() {
        return level;
    }

    public boolean isBoosted() {
        return isBoosted;
    }
}
