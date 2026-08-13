package com.pvz.models.entities.zombies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.zombies.armor.ArmorFlag;
import com.pvz.models.entities.zombies.armor.ArmorPiece;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.data.ScaledProp;
import com.pvz.models.entities.zombies.data.ZombiePropertySheet;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.entities.zombies.fsm.DeadState;
import com.pvz.models.entities.zombies.fsm.EatState;
import com.pvz.models.entities.zombies.fsm.WalkState;
import com.pvz.models.entities.zombies.fsm.ZombieState;
import com.pvz.models.entities.zombies.skills.ExplorerTorchSkill;
import com.pvz.models.entities.zombies.skills.ZombieSkill;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.tile.Tile;


public class Zombie extends Entity {


    public static final int TICKS_PER_SECOND = 10;

    private static final Random RNG = new Random();
    private static final float GLOW_CHANCE = 0.05f;

    // ── Identity ──────────────────────────────────────────────────────────────
    private final ZombiePropertySheet sheet;
    private final GameContext context;

    private float stateTime = 0f;
    // ── Grid position ─────────────────────────────────────────────────────────
    private int lastX;
    private int lastLane;

    // ── Runtime stats (scaled at spawn) ───────────────────────────────────────
    private final float maxHp;
    private float hp;
    private final float eatDpsPerTick;
    private final float speedPerTick;

    // ── Components ────────────────────────────────────────────────────────────
    private final List<ArmorPiece> armors;
    private final Map<EffectType, StatusEffect> activeEffects;
    private final List<ZombieSkill> skills;

    // ── FSM ───────────────────────────────────────────────────────────────────
    private ZombieState currentState;

    // ── Flags ─────────────────────────────────────────────────────────────────
    private boolean dead;
    private final boolean glowing;
    private boolean impAlreadyThrown = false;
    private int stolenSun;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Constructed exclusively by {@link ZombieFactory}.
     *
     * @param sheet      immutable data sheet
     * @param startX     spawn column (typically rightmost)
     * @param lane       row index [0, lanes)
     * @param armors     front-to-back armour stack
     * @param skills     special-ability plug-ins
     * @param ctx        world interface
     * @param waveIndex  0-based wave number for ScaledProp scaling
     * @param difficulty [1..5]; 3 = no modifier
     */
    public Zombie(ZombiePropertySheet sheet, float startX, int lane,
                  List<ArmorPiece> armors, List<ZombieSkill> skills,
                  GameContext ctx, int waveIndex, int difficulty) {
        this.sheet = sheet;
        this.position.set(startX, GameController.laneToWorldY(lane));
        lastX = (int) startX;
        this.armors = new ArrayList<>(armors);
        this.skills = new ArrayList<>(skills);
        this.activeEffects = new EnumMap<>(EffectType.class);
        this.context = ctx;
        this.glowing = sheet.isCanSpawnPlantFood() && RNG.nextFloat() < GLOW_CHANCE;

        float diffFactor = difficulty / 3.0f;
        float[] scaled = computeScaledStats(waveIndex);
        this.maxHp         = scaled[0] * diffFactor;
        this.hp            = this.maxHp;
        this.eatDpsPerTick = scaled[1] * diffFactor / TICKS_PER_SECOND;
        this.speedPerTick  = sheet.getSpeed() / TICKS_PER_SECOND;

        setHitbox(48f, 80f);
        velocity.set(-getEffectiveSpeedPerTick() * 1000f, 0f);
    }

    // ── TickAware ─────────────────────────────────────────────────────────────

    @Override
    public void enter() {
        currentState = new WalkState();
        currentState.onEnter(this, context);
        context.log("[Spawn] " + sheet.getAlias()
                + " entered lane " + GameController.worldYtoLane(position.y) + " at x=" + String.format("%.1f", position.x)
                + (glowing ? " [GLOWING]" : ""));
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (dead) return;
        tickStatusEffects();
        if (isParalysed()) return;
        ZombieState next = currentState.update(this, context,dt);
        if (next != currentState) {
            currentState.onExit(this, context);
            next.onEnter(this, context);
            currentState = next;
        }

        if (Math.floor(position.x) != lastX || GameController.worldYtoLane(position.y) != lastLane) {
            lastX = (int) Math.floor(position.x);
            lastLane = GameController.worldYtoLane(position.y);
            Tile tile = context.getTileAt(lastX, GameController.worldYtoLane(position.y));
            if (tile != null) {
                for (TileBehavior b : tile.getBehaviors()) {
                    b.onZombieEnter(this, tile);
                }
            }
        }

        syncHitbox();
    }

    @Override
    public FrameConfig draw() {
        if (currentState == null) {
            return null;
        }
        return currentState.draw(this, context);
    }

    /**
     * Builds the {@link FrameConfig} for the given clip label from the zombie's
     * config-driven animation (pam path + scale), falling back to the classic
     * tutorial PAM when no config is attached.
     */
    public FrameConfig drawClip(String clipLabel) {
        ZombieAnimationConfig anim = sheet.getAnimationConfig();
        String pamPath = (anim != null && anim.pamFilePath != null)
                ? anim.pamFilePath
                : "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM";
        float scale = (anim != null && anim.scale != null) ? anim.scale : 0.65f;
        return new FrameConfig(pamPath, clipLabel, stateTime,
                new Vector2(position.x, position.y), new Vector2(scale, scale), null, true);
    }

    @Override
    public void dispose() {
        activeEffects.clear();
    }

    // ── Collision ─────────────────────────────────────────────────────────────

    /**
     * Invoked by the {@link CollisionSystem} when this zombie's hitbox overlaps
     * another entity's. Overlapping a plant makes the zombie stop walking and
     * transition into {@link EatState} so it can chew through it.
     */
    @Override
    public void onCollision(Entity other) {
        if (dead || other == null) {
            return;
        }
        if (other instanceof Plant plant && !plant.isDead() && !plant.isFrozen()) {
            startEating(plant);
        }
    }

    /** Switches to {@link EatState} targeting {@code plant} if not already eating. */
    public void startEating(Plant plant) {
        if (dead || currentState instanceof EatState) {
            return;
        }
        currentState.onExit(this, context);
        EatState eat = new EatState(plant);
        eat.onEnter(this, context);
        currentState = eat;
    }

    // ── Damage API ─────────────────────────────────────────────────────────────

    /**
     * Routes damage through the armor chain then into {@link #hp}.
     *
     * @param amount    positive damage value
     * @param poisonous true → bypasses all armour (Poison Pea, etc.)
     */
    public void takeDamage(float amount, boolean poisonous) {
        if (dead || amount <= 0f) return;
        float remaining = poisonous ? amount : processArmorChain(amount);
        hp = Math.max(0f, hp - remaining);
        if (hp <= 0f && !dead) triggerDeath();
    }

    public void fire(){
        for(ZombieSkill s : skills){
            if(s instanceof ExplorerTorchSkill sk){
                sk.relight();
            }
        }
    }

    /** Convenience: non-poisonous damage. */
    public void takeDamage(float amount) {
        takeDamage(amount, false);
    }

    // ── Status-effect API ─────────────────────────────────────────────────────

    /** Applies or refreshes an effect. BURNING clears CHILL and FROZEN. */
    public void applyEffect(StatusEffect effect) {
        if (effect.getType() == EffectType.BURNING) {
            activeEffects.remove(EffectType.CHILL);
            activeEffects.remove(EffectType.FROZEN);
        }
        activeEffects.put(effect.getType(), effect);
    }

    public boolean hasEffect(EffectType type) {
        StatusEffect e = activeEffects.get(type);
        return e != null && e.isActive();
    }


    // ── Speed helper ──────────────────────────────────────────────────────────

    /** Effective speed per tick: halved under CHILL, unchanged otherwise. */
    public float getEffectiveSpeedPerTick() {
        if (hasEffect(EffectType.CHILL)) return speedPerTick * 0.5f;
        return speedPerTick;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ZombiePropertySheet getSheet()       { return sheet; }
    public GameContext getContext()        { return context; }
    public float getStateTime()            { return stateTime; }
    public float getX()                         { return position.x; }
    public void setX(float newX)               { position.x = newX; syncHitbox(); }
    public float getY()                        { return position.y; }
    public void setY(float y)            { position.y = y; syncHitbox(); }
    public float getHp()                        { return hp; }
    public float getMaxHp()                     { return maxHp; }
    public float getEatDpsPerTick()             { return eatDpsPerTick; }
    public float getSpeedPerTick()              { return speedPerTick; }
    public boolean isDead()                     { return dead; }
    public boolean isGlowing()                  { return glowing; }
    public int getStolenSun()                   { return stolenSun; }
    public void addStolenSun(int amount)        { stolenSun += amount; }
    public boolean isImpAlreadyThrown()         { return impAlreadyThrown; }
    public void markImpThrown()                 { impAlreadyThrown = true; }
    public ZombieState getCurrentState()        { return currentState; }
    public List<ZombieSkill> getSkills()        { return Collections.unmodifiableList(skills); }
    public List<ArmorPiece> getArmors()         { return Collections.unmodifiableList(armors); }
    public Map<EffectType, StatusEffect> getActiveEffects() {
        return Collections.unmodifiableMap(activeEffects);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private float[] computeScaledStats(int waveIndex) {
        float hpBase  = sheet.getHitPoints();
        float dpsBase = sheet.getEatDps();
        for (ScaledProp prop : sheet.getScaledProps()) {
            switch (prop.getKey()) {
                case "Hitpoints": hpBase  = prop.scale(hpBase,  waveIndex); break;
                case "EatDPS":    dpsBase = prop.scale(dpsBase, waveIndex); break;
                default: break;
            }
        }
        return new float[]{hpBase, dpsBase};
    }

    private float processArmorChain(float amount) {
        for (ArmorPiece armor : armors) {
            if (armor.isDestroyed()) continue;
            float overflow = armor.absorbDamage(amount);
            if (armor.isDestroyed()) {
                context.log(sheet.getAlias() + "'s "
                        + armor.getType().name() + " armour was destroyed!");
            }
            if (!armor.hasFlag(ArmorFlag.PASSDAMAGE)) {
                return overflow;
            }
            // PASSDAMAGE: armour depletes but full amount continues
        }
        return amount;
    }

    private void triggerDeath() {
        dead = true;
        currentState = new DeadState();
        if (glowing) {
            context.addPlantFood(1);
            context.log("The glowing zombie dropped a plant food! [" + sheet.getAlias() + "]");
        }
        if (stolenSun > 0) {
            context.addSun(stolenSun);
            context.log(sheet.getAlias() + " dropped " + stolenSun + " stolen sun on death!");
            stolenSun = 0;
        }
        ZombieLootService.rollAndApplyLoot(context);
        context.getGameStats().onZombieKilled();
        context.getGameStats().onZombieKilledInSeason(context.getSeasonName());
        context.removeZombie(this);
        context.log("Zombie of type " + sheet.getAlias()
                + " is dead at (" + String.format("%.1f", position.x) + "," + GameController.worldYtoLane(position.y) + ")");
    }

    private void tickStatusEffects() {
        activeEffects.entrySet().removeIf(entry -> !entry.getValue().tick());
    }

    private boolean isParalysed() {
        return hasEffect(EffectType.FROZEN)
                || hasEffect(EffectType.TRANSFORMED)
                || hasEffect(EffectType.STUN);
    }

    private void appendArmorDetails(StringBuilder sb) {
        sb.append("\n  armor:");
        boolean any = armors.stream().anyMatch(a -> !a.isDestroyed());
        if (!any) { sb.append(" (none)"); return; }
        for (ArmorPiece a : armors) {
            if (!a.isDestroyed()) {
                sb.append("\n    ").append(a.getType().name().toLowerCase())
                        .append(": ").append(String.format("%.0f", a.getCurrentHealth()));
            }
        }
    }

    private void appendEffectDetails(StringBuilder sb) {
        sb.append("\n  effects:");
        if (activeEffects.isEmpty()) { sb.append(" (none)"); return; }
        activeEffects.forEach((type, eff) ->
                sb.append("\n    ").append(type.name().toLowerCase())
                        .append(": ").append(String.format("%.1f", eff.getSecondsRemaining())).append("s"));
    }
}
