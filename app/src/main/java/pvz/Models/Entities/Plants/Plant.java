package pvz.Models.Entities.Plants;

import java.util.List;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Enums.PlantCategory;
import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Plants.Enums.PlantType;

public class Plant implements TickAware{
    private Vector2 position;
    private Vector2 velocity;

    private final PlantType type;
    private final PlantCategory category;
    private final List<PlantTag> tags;
    private Strategy strategy;
    
    private int sunCost;
    private int baseHp;
    private int baseRecharge;
    private int baseActionInterval;
    private int baseDamage;

    private int Recharge;
    private int ActionInterval;
    private int hp;
    private int level;
    private int damage;
    private boolean isBoosted;

    public Plant(Vector2 position, PlantType type, PlantCategory category, List<PlantTag> tags,
            Strategy strategy, int sunCost, int baseHp, int baseRecharge, int baseActionInterval
            , int level, boolean isBoosted) {
        this.position = position;
        this.type = type;
        this.category = category;
        this.tags = tags;
        this.strategy = strategy;
        this.sunCost = sunCost;
        this.baseHp = baseHp;
        hp=baseHp;
        this.baseRecharge = baseRecharge;
        this.baseActionInterval = baseActionInterval;
        Recharge = baseRecharge;
        ActionInterval = baseActionInterval;
        this.level = level;
        this.isBoosted = isBoosted;
    }

    public void enter(){}
    public void update(){
        strategy.defaultApply();
    }

    @Override
    public void dispose() {

    }

    public Vector2 getPosition() {
        return position;
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
        return baseHp;
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
        return hp;
    }
    public int getLevel() {
        return level;
    }
    public boolean isBoosted() {
        return isBoosted;
    }

    public void setBoosted(boolean boosted) {
        isBoosted = boosted;
    }

}
