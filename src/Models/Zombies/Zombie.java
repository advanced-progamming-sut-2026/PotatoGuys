package Models.Zombies;

import Models.DataTypes.Vector2;

import java.util.List;

public abstract class Zombie {

    private Vector2 position;
    private Vector2 velocity;

    private final ZombieType type;
    private final int cost;
    private final int baseHP;
    private final int baseDamage;
    private final float movementSpeed;

    private int HP;
    private int damage;
    private List<ZombieEffect> effects;
    private List<ZombieArmor> armors;
    
    public Zombie(Vector2 position, Vector2 velocity, ZombieType type, int cost, int baseHP, int baseDamage,
            float movementSpeed, int hP, int damage, List<ZombieEffect> effects, List<ZombieArmor> armors) {
        this.position = position;
        this.velocity = velocity;
        this.type = type;
        this.cost = cost;
        this.baseHP = baseHP;
        this.baseDamage = baseDamage;
        this.movementSpeed = movementSpeed;
        HP = hP;
        this.damage = damage;
        this.effects = effects;
        this.armors = armors;
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
    public int getBaseHP() {
        return baseHP;
    }
    public int getBaseDamage() {
        return baseDamage;
    }
    public float getMovementSpeed() {
        return movementSpeed;
    }
    public int getHP() {
        return HP;
    }
    public int getDamage() {
        return damage;
    }
    public List<ZombieEffect> getEffects() {
        return effects;
    }
    public List<ZombieArmor> getArmors() {
        return armors;
    }


}
