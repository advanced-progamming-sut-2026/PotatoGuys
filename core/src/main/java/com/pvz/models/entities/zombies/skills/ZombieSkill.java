package com.pvz.models.entities.zombies.skills;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.config.ZombieSkillConfig;
import com.pvz.models.entities.zombies.fsm.WalkState;
import com.pvz.models.entities.zombies.fsm.ZombieState;
import com.pvz.models.games.GameContext;

/**
 * A special ability modelled as a zombie FSM state — the exact zombie
 * counterpart of {@link com.pvz.models.entities.plants.actions.PlantAction}.
 *
 * <p>Each skill <em>is</em> a {@link ZombieState}:
 * <ol>
 *   <li>While the zombie {@code WalkState}s, {@link #shouldTrigger} is polled
 *       every tick. When it returns {@code true}, the zombie's current state is
 *       switched to this skill instance, so the skill's own
 *       {@link #update}/{@link #draw} take over.</li>
 *   <li>{@link #onEnter} fires the skill's game-world effect once
 *       ({@link #doExecute}).</li>
 *   <li>{@link #update} holds the skill's pose, playing its PAM clip, until the
 *       animation has finished (clip duration from {@link AnimationCatalog},
 *       falling back to {@code durationSeconds} from the skill's JSON config),
 *       then returns to {@link WalkState}.</li>
 * </ol>
 *
 * <p>Behaviour parameters come from a {@link ZombieSkillConfig} subclass parsed
 * from {@code zombie_actions.json} (like the plants' config-driven actions), so
 * tuning a skill means editing JSON, not code.
 */
public abstract class ZombieSkill extends ZombieState {

    protected final ZombieSkillConfig config;

    protected ZombieSkill(ZombieSkillConfig config) {
        this.config = config;
    }

    /**
     * Evaluated every walk tick by {@code WalkState}.
     * Should be cheap — skills with cooldowns accumulate their timing here.
     *
     * @return true if the skill is ready and its conditions are met
     */
    public abstract boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt);

    /** Performs the skill's game-world effect. Called exactly once on state entry. */
    protected abstract void doExecute(Zombie zombie, GameContext ctx);

    /** Short label shown in CLI debug output, e.g. {@code "StealSun"}, {@code "RaiseTomb"}. */
    public abstract String getName();

    /** PAM clip label this skill plays while active (from the JSON config). */
    protected String getSkillLabel() {
        return config != null && config.label != null ? config.label : "power";
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
        doExecute(zombie, ctx);
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        if (stateTime >= getHoldSeconds(zombie)) {
            return new WalkState();
        }
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        return zombie.drawClip(getSkillLabel());
    }

    // ── Animation timing ──────────────────────────────────────────────────────

    /**
     * How long the skill state stays in its pose before returning to walk:
     * the duration of the configured PAM clip when known, otherwise the JSON
     * {@code durationSeconds} fallback.
     */
    protected float getHoldSeconds(Zombie zombie) {
        ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
        if (anim != null && anim.pamFilePath != null) {
            AnimationCatalog catalog = AnimationCatalog.getInstance();
            if (catalog != null) {
                float clip = catalog.getClipDuration(anim.pamFilePath, getSkillLabel());
                if (clip > 0f) return clip;
            }
        }
        return getFallbackHoldSeconds();
    }

    /** JSON-configured fallback hold time (seconds) when the clip duration is unknown. */
    protected float getFallbackHoldSeconds() {
        return config != null && config.durationSeconds > 0f ? config.durationSeconds : 1f;
    }
}
