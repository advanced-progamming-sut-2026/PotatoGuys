package pvz.models.entities.sun;

import java.util.ArrayList;
import java.util.List;

import pvz.models.engine.TickAware;
import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.DamageKind;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;

public class Sun implements TickAware {

    private static final int TICKS_PER_SECOND = 10;
    private static final float DEFAULT_LIFESPAN_SECONDS = 8f;
    private static final int FALL_DURATION_TICKS = 50;

    private final SunType type;
    private final int col;
    private final int lane;
    private final int amount;
    private final GameContext context;

    private int ticksRemaining;
    private int fallingTicksRemaining;
    private boolean fallen;
    private boolean collected;

    /** Backward-compatible constructor (plant-produced suns, no falling). */
    public Sun(SunType type, int col, int lane, int amount) {
        this(type, col, lane, amount, false, null);
    }

    public Sun(SunType type, int col, int lane, int amount, boolean startFalling, GameContext context) {
        this.type = type;
        this.col = col;
        this.lane = lane;
        this.amount = amount;
        this.context = context;
        this.ticksRemaining = Math.max(1, Math.round(DEFAULT_LIFESPAN_SECONDS * TICKS_PER_SECOND));
        this.fallingTicksRemaining = startFalling ? FALL_DURATION_TICKS : 0;
        this.fallen = !startFalling;
    }

    @Override
    public void enter() { }

    @Override
    public void update() {
        if (collected) return;

        if (!fallen) {
            fallingTicksRemaining--;
            if (fallingTicksRemaining <= 0) {
                fallen = true;
                if (context != null) {
                    context.log("Sun reached the ground at position (" + col + ", " + lane + ")");
                }
                if (type == SunType.RADIOACTIVE) {
                    convertToNormal();
                }
            }
        } else {
            ticksRemaining--;
        }
    }

    @Override
    public void dispose() { }

    public void collect(GameContext ctx) {
        if (isDone()) return;
        collected = true;

        if (type == SunType.RADIOACTIVE && !fallen) {
            dealExplosionDamage(ctx);
        } else {
            ctx.addSun(getAmount());
        }
        ctx.removeSun(this);
    }

    private void dealExplosionDamage(GameContext ctx) {
        ctx.log("Radioactive sun exploded at position (" + col + ", " + lane + ")!");

        List<Zombie> zombiesHit = new ArrayList<>();
        List<Plant> plantsHit = new ArrayList<>();

        for (int c = col - 2; c <= col + 2; c++) {
            for (int l = lane - 2; l <= lane + 2; l++) {
                if (c < 0 || c >= ctx.getColumns() || l < 0 || l >= ctx.getLanes()) continue;
                zombiesHit.addAll(ctx.getZombiesAt(c, l));
                plantsHit.addAll(ctx.getPlantsAt(c, l));
            }
        }
        for (Zombie z : zombiesHit) z.takeDamage(150);
        for (Plant p : plantsHit) p.takeDamage(150, DamageKind.FIXED);

        List<Zombie> zombiesCenter = new ArrayList<>();
        List<Plant> plantsCenter = new ArrayList<>();

        for (int c = col - 1; c <= col + 1; c++) {
            for (int l = lane - 1; l <= lane + 1; l++) {
                if (c < 0 || c >= ctx.getColumns() || l < 0 || l >= ctx.getLanes()) continue;
                zombiesCenter.addAll(ctx.getZombiesAt(c, l));
                plantsCenter.addAll(ctx.getPlantsAt(c, l));
            }
        }
        for (Zombie z : zombiesCenter) z.takeDamage(80);
        for (Plant p : plantsCenter) p.takeDamage(80, DamageKind.FIXED);
    }

    private void convertToNormal() {
        if (context != null) {
            context.log("Radioactive sun at (" + col + ", " + lane + ") became a normal sun upon reaching the ground.");
        }
    }

    public boolean isFalling()    { return !fallen; }
    public boolean isExpired()    { return !collected && fallen && ticksRemaining <= 0; }
    public boolean isCollected()  { return collected; }
    public boolean isDone()       { return collected || isExpired(); }

    public SunType getType() { return type; }
    public int getCol()      { return col; }
    public int getLane()     { return lane; }
    public int getAmount()   { return amount > 0 ? amount : type.getAmountSun(); }
    public float getSecondsRemaining() { return Math.max(0, ticksRemaining) / (float) TICKS_PER_SECOND; }
}
