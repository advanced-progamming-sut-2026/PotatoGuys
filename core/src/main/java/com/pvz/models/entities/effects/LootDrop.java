package com.pvz.models.entities.effects;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.games.GameContext;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePot;
import com.pvz.models.user.Profile;
import com.pvz.models.user.User;

/**
 * A clickable loot drop a zombie can leave behind on death (spawned by
 * {@code ZombieLootService} when the 10% loot roll picks the coin, diamond or
 * greenhouse-pot outcome).
 *
 * <p>Life cycle:
 * <ol>
 *   <li>{@code POP} — pops straight up out of the zombie's head and falls back
 *       down to its exact start point (one smooth arc);</li>
 *   <li>{@code READY} — sits there until the player clicks it (expires after a while);</li>
 *   <li>{@code FLY} / {@code COLLECT} — the click response: coins and diamonds arc
 *       into their on-screen wallet and are banked on arrival; the pot unlocks a
 *       random locked greenhouse pot the moment it is clicked.</li>
 * </ol>
 *
 * <p>Rendered with the matching PAM (gold coin / diamond / sprout). Clicking is
 * handled by {@code GameController}, which passes the wallet's world position to
 * {@link #flyTo(float, float)} or calls {@link #collect()} for the pot.
 */
public class LootDrop extends Entity {

    /** Per-loot-type presentation data: PAM + clip label + sizes. */
    public enum LootType {
        COIN("768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM", "animation", 0.6f, 0.4f),
        DIAMOND("768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM", "idle", 0.4f, 0.3f),
        POT("768/INITIAL/ZEN_GARDEN/SPROUTDOOBER/SPROUTDOOBER.PAM", "animation", 0.7f, 0.5f),
        PLANT_FOOD("768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM", "idle", 0.6f, 0.4f);

        final String pamPath;
        final String clip;
        final float baseScale;
        final float startScale;

        LootType(String pamPath, String clip, float baseScale, float startScale) {
            this.pamPath = pamPath;
            this.clip = clip;
            this.baseScale = baseScale;
            this.startScale = startScale;
        }
    }

    /** How high above its start point the loot pops up to. */
    private static final float POP_UP_HEIGHT = 150f;
    /** Duration of the whole up-and-back-down pop. */
    private static final float POP_DURATION = 0.85f;
    private static final float FLY_DURATION = 0.50f;
    /** Uncollected loot vanishes (unbanked) after this long in READY state. */
    private static final float READY_TIMEOUT = 30f;
    /** How high the wallet-fly arc bends above the straight line. */
    private static final float FLY_ARC_HEIGHT = 140f;
    private static final float COLLECT_DURATION = 0.30f;
    private static final float COLLECT_SCALE_UP = 1.4f;

    private enum Phase { POP, READY, FLY, COLLECT, DONE }

    private final GameContext context;
    private final LootType type;
    private final int amount;

    private final Vector2 startPos = new Vector2();
    private final Vector2 flyStart = new Vector2();
    private final Vector2 flyTarget = new Vector2();
    private final Vector2 flyControl = new Vector2();

    private Phase phase;
    private float phaseTime;
    private float stateTime;
    private float scale;
    private float flyStartScale;

    private LootDrop(GameContext context, LootType type, float spawnX, float spawnY, int amount) {
        this.context = context;
        this.type = type;
        this.amount = amount;
        this.phase = Phase.POP;
        this.phaseTime = 0f;
        this.stateTime = 0f;
        this.scale = type.startScale;

        startPos.set(spawnX, spawnY);
        setPosition(spawnX, spawnY);
    }

    public static LootDrop coin(GameContext context, float spawnX, float spawnY, int amount) {
        return new LootDrop(context, LootType.COIN, spawnX, spawnY, amount);
    }

    public static LootDrop diamond(GameContext context, float spawnX, float spawnY, int amount) {
        return new LootDrop(context, LootType.DIAMOND, spawnX, spawnY, amount);
    }

    public static LootDrop pot(GameContext context, float spawnX, float spawnY) {
        return new LootDrop(context, LootType.POT, spawnX, spawnY, 0);
    }

    public static LootDrop plantFood(GameContext context, float spawnX, float spawnY) {
        return new LootDrop(context, LootType.PLANT_FOOD, spawnX, spawnY, 1);
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        phaseTime += dt;

        switch (phase) {
            case POP -> {
                // A single smooth arc: up to the peak, then back down to the exact
                // start point. sin() drives y from start -> peak -> start, so the
                // motion never overshoots and is clean on both legs.
                float t = Math.min(1f, phaseTime / POP_DURATION);
                float arc = MathUtils.sin(t * MathUtils.PI);
                position.set(startPos.x, startPos.y + POP_UP_HEIGHT * arc);
                scale = MathUtils.lerp(type.startScale, type.baseScale, arc);
                if (t >= 1f) {
                    position.set(startPos);
                    scale = type.baseScale;
                    phase = Phase.READY;
                    phaseTime = 0f;
                }
            }
            case READY -> {
                if (phaseTime >= READY_TIMEOUT) {
                    phase = Phase.DONE;
                    context.removeLootDrop(this);
                }
            }
            case FLY -> {
                float t = Math.min(1f, phaseTime / FLY_DURATION);
                float eased = Interpolation.pow2In.apply(t);
                float inv = 1f - eased;
                position.set(
                    inv * inv * flyStart.x + 2f * inv * eased * flyControl.x + eased * eased * flyTarget.x,
                    inv * inv * flyStart.y + 2f * inv * eased * flyControl.y + eased * eased * flyTarget.y);
                scale = MathUtils.lerp(flyStartScale, 0.25f, eased);
                if (t >= 1f) {
                    position.set(flyTarget);
                    phase = Phase.DONE;
                    grantReward();
                    context.removeLootDrop(this);
                }
            }
            case COLLECT -> {
                // Instant-collect types (pot): a quick celebratory pop, then vanish.
                float t = Math.min(1f, phaseTime / COLLECT_DURATION);
                scale = type.baseScale * MathUtils.lerp(1f, COLLECT_SCALE_UP, t);
                if (t >= 1f) {
                    phase = Phase.DONE;
                    context.removeLootDrop(this);
                }
            }
            case DONE -> {
            }
        }
        syncHitbox();
    }

    @Override
    public FrameConfig draw() {
        PvZ2.pamPlayer.draw(PvZ2.batch, type.pamPath, type.clip, stateTime, position.x, position.y, scale, scale, true);
        return null;
    }

    @Override
    public void dispose() {
    }

    /**
     * Starts the "collected" flight toward the wallet (coins / diamonds). The loot
     * accelerates along a bezier arc, shrinking as it nears the wallet; the reward
     * is banked only when it actually arrives.
     */
    public void flyTo(float worldX, float worldY) {
        if (phase != Phase.READY) {
            return;
        }
        flyStart.set(position);
        flyTarget.set(worldX, worldY);
        flyControl.set((flyStart.x + flyTarget.x) / 2f, (flyStart.y + flyTarget.y) / 2f + FLY_ARC_HEIGHT);
        flyStartScale = scale;
        phase = Phase.FLY;
        phaseTime = 0f;
    }

    /**
     * Instantly collects a loot type with no wallet target (the greenhouse pot):
     * the reward is granted right away and the drop pops before disappearing.
     */
    public void collect() {
        if (phase != Phase.READY) {
            return;
        }
        grantReward();
        phase = Phase.COLLECT;
        phaseTime = 0f;
    }

    /** Only fully-landed loot can be clicked. */
    public boolean isCollectable() {
        return phase == Phase.READY;
    }

    public boolean isDone() {
        return phase == Phase.DONE;
    }

    public LootType getType() {
        return type;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public int getAmount() {
        return amount;
    }

    private void grantReward() {
        User user = AppContext.getInstance().getCurrentUser();
        if (user == null || user.getProfile() == null) {
            return;
        }
        Profile profile = user.getProfile();
        switch (type) {
            case COIN -> {
                profile.addCoins(amount);
                context.log("A zombie coin drop was collected: +" + amount + " coins!");
            }
            case DIAMOND -> {
                profile.addDiamonds(amount);
                context.log("A zombie diamond drop was collected: +" + amount + " diamonds!");
            }
            case POT -> collectPot(user);
            case PLANT_FOOD -> {
                context.addPlantFood(1);
                context.log("A plant food drop was collected! +1 plant food.");
            }
        }
        user.saveUser();
    }

    /** Mirrors ZombieLootService's own resolution so the drop always touches the live instance. */
    private void collectPot(User user) {
        GreenHouse greenHouse = AppContext.getInstance().getGreenHouse();
        if (greenHouse == null) {
            greenHouse = user.getGreenHouse();
        }
        if (greenHouse == null) {
            greenHouse = new GreenHouse();
            user.setGreenHouse(greenHouse);
        }

        GreenHousePot unlocked = greenHouse.unlockRandomPot();
        if (unlocked == null) {
            // Every pot is already unlocked: don't waste the drop, give coins instead.
            user.getProfile().addCoins(50);
            context.log("All greenhouse pots were already unlocked: +50 coins instead.");
            return;
        }
        context.log("A zombie dropped a greenhouse pot: a new pot is now unlocked in the greenhouse!");
    }
}
