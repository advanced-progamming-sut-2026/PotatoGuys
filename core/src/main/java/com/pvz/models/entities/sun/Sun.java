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

    /**
     * Life cycle of a falling radioactive sun.
     *
     * <p>{@code FALLING} → {@code EXPLODING} when the player clicks it; the
     * {@code attack} clip plays, damage lands 1.5s later and the sun despawns
     * when the clip finishes.
     *
     * <p>{@code FALLING} → {@code TRANSITIONING} → {@code NORMAL} when it hits
     * the ground untouched; the {@code transition} clip plays, then the normal
     * sun idle clip loops and the sun becomes collectible like a regular sun.
     */
    public enum RadioactiveState {
        FALLING, EXPLODING, TRANSITIONING, NORMAL
    }

    private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
    private static final String SUN_CLIP = "animation";
    private static final String TRANSITION_RED_CLIP = "transition_red";
    private static final String RED_CLIP = "red";
    private static final String RADIOACTIVE_SUN_PAM = "768/FULL/EFFECTS/SUN_BOMB/SUN_BOMB.PAM";
    private static final String RADIOACTIVE_ATTACK_CLIP = "attack";
    private static final String RADIOACTIVE_TRANSITION_CLIP = "transition";
    private static final String RADIOACTIVE_NORMAL_SUN_IDLE_CLIP = "normalSunIdle";

    private static final float DEFAULT_LIFESPAN_SECONDS = 50f;
    private static final float DEFAULT_FALL_SPEED = 70f;
    private static final float TRANSITION_RED_FALLBACK_SECONDS = 0.5333f;
    private static final float EXPLOSION_DAMAGE_DELAY_SECONDS = 1.5f;
    private static final float EXPLOSION_FALLBACK_SECONDS = 2.3f;
    private static final float RADIOACTIVE_TRANSITION_FALLBACK_SECONDS = 0.5333f;
    private static final int EXPLOSION_DAMAGE = 80;
    private static final int EXPLOSION_RADIUS_TILES = 1;

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

    // ── Radioactive sun state machine ──────────────────────────────────────
    // Drives the "attack" explosion sequence and the "transition" → normal sun
    // conversion. Only meaningful when {@code type == SunType.RADIOACTIVE}.
    private RadioactiveState radioactiveState;
    private float radioactiveStateTime;
    private boolean explosionDamageDealt;

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
        this.radioactiveState = type == SunType.RADIOACTIVE ? RadioactiveState.FALLING : null;
        this.radioactiveStateTime = 0f;
        this.explosionDamageDealt = false;
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
            drawRadioactive(frameConfigs);
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

        if (type == SunType.RADIOACTIVE) {
            updateRadioactive(dt);
            return;
        }

        if (!fallen) {
            position.add(0, -fallSpeed * dt);
            syncHitbox();
            if (position.y < targetPos.y) {
                fallen = true;
                if (context != null) {
                    context.log("Sun reached the ground at position (" + position.x + ", " + position.y + ")");
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (context != null) {
            context.removeSun(this);
        }
    }

    public void collect(GameContext ctx) {
        if (isDone())
            return;
        if (type == SunType.RADIOACTIVE) {
            switch (radioactiveState) {
                case FALLING -> {
                    startExplosion();
                    return;
                }
                case NORMAL -> {
                    collected = true;
                    ctx.addSun(getAmount());
                    ctx.removeSun(this);
                    return;
                }
                default -> {
                    // Exploding or mid-transition: ignore further clicks.
                    return;
                }
            }
        }
        collected = true;
        ctx.addSun(getAmount());
        ctx.removeSun(this);
    }

    // ── Radioactive sun helpers ───────────────────────────────────────────────

    private void drawRadioactive(List<FrameConfig> frameConfigs) {
        switch (radioactiveState) {
            case FALLING -> frameConfigs.add(new FrameConfig(RADIOACTIVE_SUN_PAM, SUN_CLIP, stateTime, position,
                    new Vector2(0.75f, 0.75f), null, true));
            case EXPLODING -> frameConfigs.add(new FrameConfig(RADIOACTIVE_SUN_PAM, RADIOACTIVE_ATTACK_CLIP,
                    radioactiveStateTime, position, new Vector2(0.75f, 0.75f), null, false));
            case TRANSITIONING -> frameConfigs.add(new FrameConfig(RADIOACTIVE_SUN_PAM, RADIOACTIVE_TRANSITION_CLIP,
                    radioactiveStateTime, position, new Vector2(0.65f, 0.65f), null, false));
            case NORMAL -> frameConfigs.add(new FrameConfig(RADIOACTIVE_SUN_PAM, RADIOACTIVE_NORMAL_SUN_IDLE_CLIP,
                    stateTime, position, new Vector2(0.65f, 0.65f), null, true));
        }
    }

    private void updateRadioactive(float dt) {
        switch (radioactiveState) {
            case FALLING -> {
                position.add(0, -fallSpeed * dt);
                syncHitbox();
                if (position.y < targetPos.y) {
                    fallen = true;
                    if (context != null) {
                        context.log("Sun reached the ground at position (" + position.x + ", " + position.y + ")");
                    }
                    beginConvertToNormal();
                }
            }
            case EXPLODING -> {
                radioactiveStateTime += dt;
                if (!explosionDamageDealt && radioactiveStateTime >= EXPLOSION_DAMAGE_DELAY_SECONDS) {
                    explosionDamageDealt = true;
                    dealExplosionDamage();
                }
                if (radioactiveStateTime >= getExplosionDuration()) {
                    dispose();
                }
            }
            case TRANSITIONING -> {
                radioactiveStateTime += dt;
                if (radioactiveStateTime >= getTransitionDuration()) {
                    setRadioactiveState(RadioactiveState.NORMAL);
                    if (context != null) {
                        context.log("Radioactive sun at (" + col + ", " + lane
                                + ") finished converting to a normal sun.");
                    }
                }
            }
            case NORMAL -> {
                // Landed and converted: behaves like a regular sun (collectible,
                // lifespan expiry handled by isExpired()).
            }
        }
    }

    private void setRadioactiveState(RadioactiveState next) {
        radioactiveState = next;
        radioactiveStateTime = 0f;
    }

    private void startExplosion() {
        setRadioactiveState(RadioactiveState.EXPLODING);
        if (context != null) {
            context.log("Radioactive sun exploded at position (" + col + ", " + lane + ")!");
        }
    }

    private void beginConvertToNormal() {
        if (context != null) {
            context.log("Radioactive sun at (" + col + ", " + lane
                    + ") became a normal sun upon reaching the ground.");
        }
        setRadioactiveState(RadioactiveState.TRANSITIONING);
    }

    /**
     * Deals {@value #EXPLOSION_DAMAGE} damage to every zombie and plant inside
     * the 3×3 tile area centered on this sun.
     */
    private void dealExplosionDamage() {
        if (context == null)
            return;
        context.log("Radioactive sun dealt " + EXPLOSION_DAMAGE
                + " damage in a 3x3 area around (" + col + ", " + lane + ")!");

        List<Zombie> zombiesHit = new ArrayList<>();
        List<Plant> plantsHit = new ArrayList<>();

        for (int c = col - EXPLOSION_RADIUS_TILES; c <= col + EXPLOSION_RADIUS_TILES; c++) {
            if (c < 0 || c >= context.getMap().getColumns())
                continue;
            for (int l = lane - EXPLOSION_RADIUS_TILES; l <= lane + EXPLOSION_RADIUS_TILES; l++) {
                if (l < 0 || l >= context.getMap().getLanes())
                    continue;
                zombiesHit.addAll(context.getZombiesAt(c, l));
                plantsHit.addAll(context.getPlantsAt(c, l));
            }
        }
        for (Zombie z : zombiesHit)
            z.takeDamage(EXPLOSION_DAMAGE);
        for (Plant p : plantsHit)
            p.takeDamage(EXPLOSION_DAMAGE, DamageKind.FIXED);
    }

    /** Duration of the "attack" clip (the whole explosion sequence). */
    private float getExplosionDuration() {
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        float d = catalog != null ? catalog.getClipDuration(RADIOACTIVE_SUN_PAM, RADIOACTIVE_ATTACK_CLIP) : -1f;
        return d > 0f ? d : EXPLOSION_FALLBACK_SECONDS;
    }

    /** Duration of the "transition" clip (bomb → normal sun visual). */
    private float getTransitionDuration() {
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        float d = catalog != null ? catalog.getClipDuration(RADIOACTIVE_SUN_PAM, RADIOACTIVE_TRANSITION_CLIP) : -1f;
        return d > 0f ? d : RADIOACTIVE_TRANSITION_FALLBACK_SECONDS;
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
        if (collected || isExpired())
            return true;
        return type == SunType.RADIOACTIVE && radioactiveState == RadioactiveState.EXPLODING
                && radioactiveStateTime >= getExplosionDuration();
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