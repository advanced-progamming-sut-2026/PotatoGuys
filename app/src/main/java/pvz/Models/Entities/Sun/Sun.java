package pvz.Models.Entities.Sun;

import pvz.Models.Engine.TickAware;
import pvz.Models.Games.GameContext;

public class Sun implements TickAware {

    private static final int TICKS_PER_SECOND = 10;
    private static final float DEFAULT_LIFESPAN_SECONDS = 8f;

    private final SunType type;
    private final int col;
    private final int lane;
    private final int amount;
    private int ticksRemaining;
    private boolean collected;

    public Sun(SunType type, int col, int lane, int amount) {
        this(type, col, lane, amount, DEFAULT_LIFESPAN_SECONDS);
    }

    public Sun(SunType type, int col, int lane, int amount, float lifespanSeconds) {
        this.type = type;
        this.col = col;
        this.lane = lane;
        this.amount = amount;
        this.ticksRemaining = Math.max(1, Math.round(lifespanSeconds * TICKS_PER_SECOND));
    }

    @Override
    public void enter() { }

    @Override
    public void update() {
        if (collected) return;
        ticksRemaining--;
    }

    @Override
    public void dispose() { }

    /** Collects this sun, crediting the player's wallet through {@code ctx}. No-op once expired/collected. */
    public void collect(GameContext ctx) {
        if (isDone()) return;
        collected = true;
        ctx.addSun(getAmount());
    }

    public boolean isExpired()  { return !collected && ticksRemaining <= 0; }
    public boolean isCollected(){ return collected; }
    /** True once this sun should be removed from the engine (collected or timed out). */
    public boolean isDone()     { return collected || isExpired(); }

    public SunType getType() { return type; }
    public int getCol()      { return col; }
    public int getLane()     { return lane; }
    public int getAmount()   { return amount > 0 ? amount : type.getAmountSun(); }
    public float getSecondsRemaining() { return Math.max(0, ticksRemaining) / (float) TICKS_PER_SECOND; }
}
