package com.pvz.models.entities.sun;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.view.game.GameScreen;

public class Sun extends Entity {
    public enum SunOwner { PLANT, ZOMBIE }

    private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
    private static final String SUN_CLIP = "animation";
    private static final String TRANSITION_RED_CLIP = "transition_red";
    private static final String RED_CLIP = "red";
    private static final String RADIOACTIVE_SUN_PAM = "768/FULL/EFFECTS/SUN_BOMB/SUN_BOMB.PAM";

    private static final float DEFAULT_LIFESPAN_SECONDS = 50f;
    private static final float DEFAULT_FALL_SPEED = 70f;
    private static final float TRANSITION_RED_FALLBACK_SECONDS = 0.5333f;

    private final SunType type;
    private final int col;
    private final int lane;
    private final int amount;
    private final GameContext context;
    private SunOwner owner = SunOwner.PLANT;

    private float fallSpeed;

    private boolean fallen;
    private boolean collected;

    private float stateTime;
    private Vector2 targetPos;

    // ── Ra steal animation ───────────────────────────────────────────────────
    // While a Ra zombie is stealing this sun, the drawn clip is overridden from
    // the normal "animation" clip to "transition_red" (one shot) and then "red"
    // (held/looped) until the steal completes.
    private boolean stealing;
    private float stealStateTime;
    private float stealRedHoldSeconds = 2f;

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
        this.fallen = !startFalling;
        this.stateTime = 0;
        this.fallSpeed = DEFAULT_FALL_SPEED;
        if (startFalling) {
            position.set(GameController.colToWorldX(col), GameScreen.SCREEN_HEIGHT);
            targetPos = new Vector2(GameController.colToWorldX(col), GameController.laneToWorldY(lane));
        } else {
            position.set(GameController.colToWorldX(col), GameController.laneToWorldY(lane));
            targetPos = new Vector2(position);
        }
        velocity.set(0f, -fallSpeed);
        setHitbox(52f, 52f);
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        if (stealing) {
            // Ra steal sequence: one-shot transition_red, then the red clip looped
            // for the rest of the steal window.
            float transitionRedDuration = getTransitionRedDuration();
            if (stealStateTime < transitionRedDuration) {
                frameConfigs.add(new FrameConfig(SUN_PAM, TRANSITION_RED_CLIP, stealStateTime, position,
                        new Vector2(0.65f, 0.65f), null, false));
            } else {
                frameConfigs.add(new FrameConfig(SUN_PAM, RED_CLIP, stealStateTime - transitionRedDuration, position,
                        new Vector2(0.65f, 0.65f), null, true));
            }
        } else if (type == SunType.NORMAL) {
            frameConfigs.add(
                    new FrameConfig(SUN_PAM, SUN_CLIP, stateTime, position, new Vector2(0.65f, 0.65f), null, true));
        } else if (type == SunType.SPECIAL) {
            frameConfigs.add(
                    new FrameConfig(SUN_PAM, SUN_CLIP, stateTime, position, new Vector2(0.75f, 0.75f), null, true));
        } else {
            frameConfigs.add(
                    new FrameConfig(RADIOACTIVE_SUN_PAM, SUN_CLIP, stateTime, position, new Vector2(0.75f, 0.75f), null,
                            true));
        }
        return frameConfigs;
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (stealing) {
            stealStateTime += dt;
        }
        if (collected)
            return;

        if (!fallen) {
            position.add(0, -fallSpeed * dt);
            syncHitbox();
            if (position.y < targetPos.y) {
                fallen = true;
                if (context != null) {
                    context.log("Sun reached the ground at position (" + position.x + ", " + position.y + ")");
                }
                if (type == SunType.RADIOACTIVE) {
                    convertToNormal();
                }
            }
        }
    }

    @Override
    public void dispose() {
    }

    public void collect(GameContext ctx) {
        if (isDone())
            return;
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
                if (c < 0 || c >= ctx.getMap().getColumns() || l < 0 || l >= ctx.getMap().getLanes())
                    continue;
                zombiesHit.addAll(ctx.getZombiesAt(c, l));
                plantsHit.addAll(ctx.getPlantsAt(c, l));
            }
        }
        for (Zombie z : zombiesHit)
            z.takeDamage(150);
        for (Plant p : plantsHit)
            p.takeDamage(150, DamageKind.FIXED);

        List<Zombie> zombiesCenter = new ArrayList<>();
        List<Plant> plantsCenter = new ArrayList<>();

        for (int c = col - 1; c <= col + 1; c++) {
            for (int l = lane - 1; l <= lane + 1; l++) {
                if (c < 0 || c >= ctx.getMap().getColumns() || l < 0 || l >= ctx.getMap().getLanes())
                    continue;
                zombiesCenter.addAll(ctx.getZombiesAt(c, l));
                plantsCenter.addAll(ctx.getPlantsAt(c, l));
            }
        }
        for (Zombie z : zombiesCenter)
            z.takeDamage(80);
        for (Plant p : plantsCenter)
            p.takeDamage(80, DamageKind.FIXED);
    }

    private void convertToNormal() {
        if (context != null) {
            context.log("Radioactive sun at (" + col + ", " + lane + ") became a normal sun upon reaching the ground.");
        }
    }

    // ── Ra steal animation API ────────────────────────────────────────────────

    /**
     * Kicks off the Ra steal animation: the sun plays the {@code transition_red}
     * clip once, then holds the {@code red} clip for {@code redHoldSeconds}.
     *
     * @param redHoldSeconds how long the {@code red} clip is shown after the
     *                       transition finishes (Ra adds this on top of the
     *                       transition duration before stealing the sun)
     */
    public void startStealAnimation(float redHoldSeconds) {
        stealing = true;
        stealStateTime = 0f;
        stealRedHoldSeconds = redHoldSeconds;
    }

    /** Resets the steal animation, restoring the normal sun clip. */
    public void stopStealAnimation() {
        stealing = false;
        stealStateTime = 0f;
    }

    public boolean isStealing() {
        return stealing;
    }

    /** Seconds already spent inside the steal animation (transition + red). */
    public float getStealStateTime() {
        return stealStateTime;
    }

    /** Duration of the one-shot {@code transition_red} clip. */
    public float getTransitionRedDuration() {
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        float d = catalog != null ? catalog.getClipDuration(SUN_PAM, TRANSITION_RED_CLIP) : -1f;
        return d > 0f ? d : TRANSITION_RED_FALLBACK_SECONDS;
    }

    /** True once the transition clip plus the red hold have fully played. */
    public boolean isStealAnimationComplete() {
        return stealing && stealStateTime >= getTransitionRedDuration() + stealRedHoldSeconds;
    }

    public boolean isFalling() {
        return !fallen;
    }

    public boolean isExpired() {
        return !collected && fallen && stateTime > DEFAULT_LIFESPAN_SECONDS;
    }

    public boolean isCollected() {
        return collected;
    }

    public boolean isDone() {
        return collected || isExpired();
    }

    public SunType getType() {
        return type;
    }

    public int getCol() {
        return col;
    }

    public int getLane() {
        return lane;
    }

    public int getAmount() {
        return amount > 0 ? amount : type.getAmountSun();
    }

    public Vector2 getCurrentPos() {
        return position;
    }

    public SunOwner getOwner() {
        return owner;
    }

    public void setOwner(SunOwner owner) {
        this.owner = owner;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }
}
