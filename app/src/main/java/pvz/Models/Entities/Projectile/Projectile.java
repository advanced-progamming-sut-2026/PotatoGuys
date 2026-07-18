package pvz.Models.Entities.Projectile;

import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.effects.EffectType;
import pvz.Models.Entities.Zombies.effects.StatusEffect;
import pvz.Models.Games.GameContext;

/**
 * A single, data-configured travelling projectile fired by a plant.
 *
 * <p>Mirrors the "one concrete class, behaviour from data" philosophy used by
 * {@code Zombie}/{@code Plant}: every plant bullet (Pea, Snow Pea, Fire
 * Peashooter, Cactus spike, homing bolt, ...) is this same class, configured
 * with a damage amount, an optional pierce count, an optional chill effect,
 * and an optional homing lock — rather than one Java subclass per bullet
 * flavor.
 */
public class Projectile implements TickAware {

    private static final float COLS_PER_TICK = 1.0f;
    private static final float HIT_RADIUS = 0.5f;
    private static final int CHILL_DURATION_TICKS = 3 * Plant.TICKS_PER_SECOND;

    private final GameContext context;
    private final ProjectileType type;
    private final int lane;
    private float col;
    private final float damage;
    private final boolean poisonous;
    private final boolean chills;
    private int pierceRemaining;
    private final Zombie homingTarget; // non-null = always-hit, ignores lane travel

    private boolean spent;

    public Projectile(GameContext context, ProjectileType type, int lane, float startCol,
                       float damage, boolean poisonous, boolean chills, int pierceCount,
                       Zombie homingTarget) {
        this.context = context;
        this.type = type;
        this.lane = lane;
        this.col = startCol;
        this.damage = damage;
        this.poisonous = poisonous;
        this.chills = chills;
        this.pierceRemaining = Math.max(0, pierceCount);
        this.homingTarget = homingTarget;
    }

    @Override
    public void enter() { }

    @Override
    public void update() {
        advance();
    }

    /** Moves the projectile one tick forward and resolves any collision. */
    public void advance() {
        if (spent) return;
        if (homingTarget != null) {
            if (homingTarget.isDead()) { spent = true; return; }
            onCollide(homingTarget);
            return;
        }
        col += COLS_PER_TICK;
        
        // Notify tile of potential hit
        if (col >= 0 && col < context.getColumns()) {
            context.getTileAt(col, lane).processHit(this);
            if (spent) return;
        }

        if (col > context.getColumns() + 1) { spent = true; return; }
        Zombie nearest = null;
        float bestDistance = Float.MAX_VALUE;
        for (Zombie z : context.getZombiesInLane(lane)) {
            float distance = Math.abs(z.getX() - col);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = z;
            }
        }
        if (nearest != null && bestDistance <= HIT_RADIUS) {
            onCollide(nearest);
        }
    }

    private void onCollide(Zombie zombie) {
        zombie.takeDamage(damage, poisonous);
        if (chills) {
            zombie.applyEffect(new StatusEffect(EffectType.CHILL, CHILL_DURATION_TICKS));
        }
        if (pierceRemaining > 0) {
            pierceRemaining--;
        } else {
            spent = true;
        }
    }

    public void spend() { spent = true; }

    @Override
    public void dispose() { }

    public boolean isSpent()        { return spent; }
    public ProjectileType getType() { return type; }
    public int getLane()            { return lane; }
    public float getCol()           { return col; }
    public float getDamage()        { return damage; }
}
