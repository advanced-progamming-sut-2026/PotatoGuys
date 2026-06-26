package pvz.Models.Zombies;

import pvz.Models.DataTypes.Vector2;

import java.util.List;

public abstract class Zombie {

    private Vector2 position;
    private Vector2 velocity;

    private final ZombieType type;
    private final int cost;
    private final float baseHp;
    private final float baseDamage;
    private final float movementSpeed;

    private float hp;
    private float damage;
    private List<ZombieEffect> effects;
    private List<ZombieArmor> armors;
    
    public Zombie(Vector2 position, Vector2 velocity, ZombieType type, int cost, float baseHp, float baseDamage,
            float movementSpeed, List<ZombieEffect> effects, List<ZombieArmor> armors) {
        this.position = position;
        this.velocity = velocity;
        this.type = type;
        this.cost = cost;
        this.baseHp = baseHp;
        this.baseDamage = baseDamage;
        this.movementSpeed = movementSpeed;
        this.damage = baseDamage;
        this.effects = effects;
        this.armors = armors;
    }

    public void takeDamage(float amount){
        hp-=amount;
        if (hp<0){
            hp=0;
        }
    }

    public Vector2 getPosition() {
        return position;
    }
    public Vector2 getVelocity() {
        return velocity;
    }
    public ZombieType getType() {
        return type;
    }
    public int getCost() {
        return cost;
    }
    public float getBaseHP() {
        return baseHp;
    }
    public float getBaseDamage() {
        return baseDamage;
    }
    public float getMovementSpeed() {
        return movementSpeed;
    }
    public float getHP() {
        return hp;
    }
    public float getDamage() {
        return damage;
    }
    public List<ZombieEffect> getEffects() {
        return effects;
    }
    public List<ZombieArmor> getArmors() {
        return armors;
    }


}
