
package com.pvz.models.entities.zombies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.Hitbox;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PumpkinShield;
import com.pvz.models.entities.zombies.armor.ArmorFlag;
import com.pvz.models.entities.zombies.armor.ArmorPiece;
import com.pvz.models.entities.zombies.armor.ArmorType;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.data.ScaledProp;
import com.pvz.models.entities.zombies.data.ZombiePropertySheet;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.entities.zombies.fsm.*;
import com.pvz.models.entities.zombies.skills.ExplorerTorchSkill;
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
    private final List<ZombieState> skills;

    // ── FSM ───────────────────────────────────────────────────────────────────
    private ZombieState currentState;
    /** Optional override for the state {@link #enter()} starts in; see {@link #setPendingInitialState}. */
    private ZombieState pendingInitialState;

    // ── Flags ─────────────────────────────────────────────────────────────────
    private boolean dead;
    private final boolean glowing;
    private boolean frozen;
    private float frozenDuration;
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
                  List<ArmorPiece> armors, List<ZombieState> skills,
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

        setHitbox(new Hitbox(this, position.x, position.y, 48f, 80f) {
            @Override
            public void onCollision(Hitbox onHit) {
                if (dead) {
                    return;
                }
                if (onHit.getOwner() instanceof Plant plant && !plant.isDead() && !plant.isFrozen()) {
                    Plant shield = PumpkinShield.shieldFor(plant, context);
                    startEating(shield != null ? shield : plant);
                }
            }
        });
        velocity.set(-getEffectiveSpeedPerTick() * 1000f, 0f);

        frozen=false;
        frozenDuration=0;
    }

    // ── TickAware ─────────────────────────────────────────────────────────────

    @Override
    public void enter() {
        currentState = pendingInitialState != null ? pendingInitialState : new WalkState();
        currentState.onEnter(this, context);
        context.log("[Spawn] " + sheet.getAlias()
            + " entered lane " + GameController.worldYtoLane(position.y) + " at x=" + String.format("%.1f", position.x)
            + (glowing ? " [GLOWING]" : ""));
    }

    /**
     * Overrides the FSM state this zombie starts in, instead of the default
     * {@link WalkState}. Must be called before the zombie's first {@code enter()}
     * tick — i.e. right after construction, before handing it to
     * {@link GameContext#spawnZombie(Zombie)} — since {@code enter()} runs on
     * the next engine tick, not synchronously in the constructor.
     *
     * <p>Used by sandstorm-driven spawns ({@code Wave#spawnZombie}) to start a
     * zombie inside {@link com.pvz.models.entities.zombies.fsm.SandstormCarryState}
     * so it's carried in from off-map instead of appearing mid-lawn.
     */
    public void setPendingInitialState(ZombieState state) {
        this.pendingInitialState = state;
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (dead) return;

        updateStatusEffects(dt);
        //if (isParalysed()) return;
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

    public void changeState(ZombieState state){
        if (currentState!=null) currentState.onExit(this,context);
        this.currentState=state;
        currentState.onExit(this,context);
    }

    /** Replaces the current FSM state cleanly (onExit → onEnter). */
    public void setState(ZombieState state){
        if (currentState != null) currentState.onExit(this, context);
        currentState = state;
        if (currentState != null) currentState.onEnter(this, context);
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
        return new FrameConfig(pamPath, resolveClipLabel(anim, newspaperClipLabel(clipLabel)), stateTime,
            new Vector2(position.x, position.y), new Vector2(scale, scale),
            buildPartsVisibility(), true);
    }

    /**
     * Resolves the requested clip label to one that actually exists in the
     * sheet. Not every sheet defines the classic clips (some bosses/fishermen
     * play {@code intro}/{@code idle}/{@code special} instead of
     * {@code walk}/{@code eat}), and requesting a missing clip makes the
     * renderer throw. When the label is absent we fall back to {@code idle} and
     * finally to the first clip the sheet provides.
     *
     * <p>Purely data-driven: the clip list comes from the config, never from a
     * graphics call, so this runs unchanged on a headless server.
     */
    private String resolveClipLabel(ZombieAnimationConfig anim, String requested) {
        List<String> clips = anim != null ? anim.availableClips : null;
        if (clips == null || clips.isEmpty() || clips.contains(requested)) return requested;
        for (String candidate : new String[] { "idle", "idle2", "default", "" }) {
            if (candidate != null && clips.contains(candidate)) return candidate;
        }
        return clips.get(0);
    }

    /**
     * While the newspaper armour is alive the sheet plays its dedicated
     * {@code *_newspaper} clips (the newspaper parts exist only there). Once the
     * armour is destroyed the zombie falls back to the base clips, which show
     * it without the paper.
     */
    private String newspaperClipLabel(String clipLabel) {
        if (!hasAliveArmor(ArmorType.NEWSPAPER)) return clipLabel;
        switch (clipLabel) {
            case "walk": return "walk_newspaper";
            case "eat":  return "eat_newspaper";
            case "idle": return "idle_newspaper";
            default:     return clipLabel;
        }
    }

    private boolean hasAliveArmor(ArmorType type) {
        for (ArmorPiece armor : armors) {
            if (!armor.isDestroyed() && armor.getType() == type) return true;
        }
        return false;
    }

    /**
     * Builds the PAM part-visibility map for this zombie's current armour state.
     *
     * <p>Every living armour piece contributes its three damage-layer part names
     * (from the armour's {@code ArmorLayers} data): the layer matching the
     * piece's current {@link ArmorPiece#getLayerIndex()} is forced visible
     * ({@code true}) while the other layers are forced hidden ({@code false}),
     * so the armour visually cracks as its health drops. Destroyed pieces
     * contribute nothing, making them fall off the zombie entirely.
     *
     * <p>On top of the layer swap, {@link ArmorType#pamContainerName()} may add
     * a nested container part to force visible (its name carries the ARMOR flag,
     * so libPVZ would otherwise cull it together with its children), and the
     * {@code pamAliveParts()}/{@code pamCriticalParts()} extras pin parts such
     * as the newspaper zombie's hand (always) and flame (critical layer only).
     *
     * @return the visibility map, or {@code null} when there is no living armour
     *         to display (basic zombies / fully destroyed armour)
     */
    private Map<String, Boolean> buildPartsVisibility() {
        if (armors.isEmpty()) return null;
        Map<String, Boolean> visibility = null;
        for (ArmorPiece armor : armors) {
            if (armor.isDestroyed()) continue;
            String[] layers = armor.getType().pamLayers();
            if (layers == null) continue;
            if (visibility == null) visibility = new HashMap<>();
            int layer = Math.min(armor.getLayerIndex(), layers.length - 1);
            for (int i = 0; i < layers.length; i++) {
                visibility.put(layers[i], i == layer);
            }
            // Some sheets nest the layer parts under a container part whose
            // name carries the ARMOR flag; libPVZ culls flagged parts unless
            // revealed, and culling the container hides its children too, so
            // force the container visible together with the current layer.
            String container = armor.getType().pamContainerName();
            if (container != null) visibility.put(container, true);
            for (String alive : armor.getType().pamAliveParts()) visibility.put(alive, true);
            for (String crit : armor.getType().pamCriticalParts()) visibility.put(crit, layer == layers.length - 1);
        }
        return visibility;
    }

    @Override
    public void dispose() {
        activeEffects.clear();
    }

    // ── Collision ─────────────────────────────────────────────────────────────

    /** Switches to {@link EatState} targeting {@code plant} if not already eating. */
    public void startEating(Plant plant) {
        if (dead || currentState instanceof EatState || currentState instanceof FrozenState) {
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
        for(ZombieState s : skills){
            if(s instanceof ExplorerTorchSkill sk){
                sk.relight();
            }
        }
    }

    public void setFrozen(float duration){
        FrameConfig frameConfig = currentState.draw(this,context);
        currentState=new FrozenState(currentState,currentState.getStateTime(),
            duration,frameConfig.pamPath,frameConfig.label,frameConfig.partsVisibility);
        currentState.onEnter(this,context);
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
    public GameContext getContext()             { return context; }
    public float getStateTime()                 { return stateTime; }
    public float getX()                         { return position.x; }
    public void setX(float newX)                { position.x = newX; syncHitbox(); }
    public float getY()                         { return position.y; }
    public void setY(float y)                   { position.y = y; syncHitbox(); }
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
    public List<ZombieState> getSkills()        { return Collections.unmodifiableList(skills); }
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

    private void updateStatusEffects(float dt) {
        activeEffects.entrySet().removeIf(entry -> !entry.getValue().update(dt));
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
                .append(": ").append(String.format("%.1f", eff.getRemainingDuration())).append("s"));
    }
}


