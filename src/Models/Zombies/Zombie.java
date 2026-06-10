package Models.Zombies;

import Models.DataTypes.Vector2;
import Models.Enums.ZombieArmor;
import Models.Enums.ZombieEffect;

import java.util.List;

public abstract class Zombie {

    private Vector2 position;
    private final String name;
    private final int cost;
    private final int baseHealth;
    private final float movementSpeed;

    private int health;
    private List<ZombieEffect> effects;
    private List<ZombieArmor> armors;

    public Zombie(Vector2 position, String name, int cost, int baseHealth,float movementSpeed
            ,List<ZombieEffect> effects, List<ZombieArmor> armors){
        this.position=position;
        this.name=name;
        this.cost=cost;
        this.baseHealth=baseHealth;
        this.health=baseHealth;
        this.movementSpeed=movementSpeed;
        this.effects=effects;
        this.armors=armors;
    }

    public void takeDamage(int amount){

    }

    public Vector2 getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public int getHealth() {
        return health;
    }

    public List<ZombieEffect> getEffects() {
        return effects;
    }

    public List<ZombieArmor> getArmors() {
        return armors;
    }
}
