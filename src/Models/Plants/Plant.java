package Models.Plants;

import java.util.List;

import Models.DataTypes.Vector2;
import Models.Engine.TickAware;
import Models.Plants.Enums.PlantCategory;
import Models.Plants.Enums.PlantTag;
import Models.Plants.Enums.PlantType;

public abstract class Plant implements TickAware{
    private Vector2 position;
    private Vector2 velocity;

    private final PlantType type;
    private final PlantCategory category;
    private final List<PlantTag> tags;
    private Strategy strategy;
    
    private int sunCost;
    private int baseHP;
    private int baseRecharge;
    private int baseActionInterval;
    private int baseDamage;

    private int Recharge;
    private int ActionInterval;
    private int HP;
    private int level;
    private int damage;
    private boolean isBoosted;

    public Plant(Vector2 position, Vector2 speed, PlantType type, PlantCategory category, List<PlantTag> tags,
            Strategy strategy, int sunCost, int baseHP, int baseRecharge, int baseActionInterval, int recharge,
            int actionInterval, int hP, int level, boolean isBoosted) {
        this.position = position;
        this.speed = speed;
        this.type = type;
        this.category = category;
        this.tags = tags;
        this.strategy = strategy;
        this.sunCost = sunCost;
        this.baseHP = baseHP;
        this.baseRecharge = baseRecharge;
        this.baseActionInterval = baseActionInterval;
        Recharge = recharge;
        ActionInterval = actionInterval;
        HP = hP;
        this.level = level;
        this.isBoosted = isBoosted;
    }
    public Vector2 getPosition() {
        return position;
    }
    public Vector2 getSpeed() {
        return speed;
    }
    public PlantType getType() {
        return type;
    }
    public PlantCategory getCategory() {
        return category;
    }
    public List<PlantTag> getTags() {
        return tags;
    }
    public Strategy getStrategy() {
        return strategy;
    }
    public int getSunCost() {
        return sunCost;
    }
    public int getBaseHP() {
        return baseHP;
    }
    public int getBaseRecharge() {
        return baseRecharge;
    }
    public int getBaseActionInterval() {
        return baseActionInterval;
    }
    public int getRecharge() {
        return Recharge;
    }
    public int getActionInterval() {
        return ActionInterval;
    }
    public int getHP() {
        return HP;
    }
    public int getLevel() {
        return level;
    }
    public boolean isBoosted() {
        return isBoosted;
    }
    public void onFirstTick(){}
    public void onTick(){
        strategy.defaultApply();
    }

}
