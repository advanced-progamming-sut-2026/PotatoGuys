package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieActionConfig;
import com.pvz.models.games.GameContext;

/**
 * Default movement state — the zombie walks left across the lawn.
 *
 * <p>
 * Each tick the zombie advances by its effective speed (accounting for
 * chill/slow effects). Eating is resolved by the collision system: when the
 * zombie's hitbox overlaps a plant it is handed to {@link EatState}. This state
 * only walks and checks the zombie's own skills:
 *
 * <ol>
 * <li><b>Boundary check</b> — if {@code x ≤ 0} trigger the lawn-mower.</li>
 * <li><b>Skill check</b> — iterate the zombie's skill list; if any skill's
 * {@link ZombieSkill#shouldTrigger} returns {@code true}, transition
 * directly into <em>that skill's state</em>. The skill runs its own
 * {@code update}/{@code draw} (playing its clip) until the animation
 * finishes, then hands the zombie back to {@code WalkState}.</li>
 * </ol>
 */
public class WalkState extends ZombieState {

    public WalkState() {
        super(null);
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        return false;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {

    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        // no visual cue needed for walking
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        advancePosition(zombie, dt);

        ZombieState skillTransition = checkForSkill(zombie, ctx, dt);
        if (skillTransition != null)
            return skillTransition;

        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Walking";
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        ZombieActionConfig walk = zombie.getSheet().walkConfig;
        return zombie.drawClip(walk != null ? walk.label : "walk");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Move the zombie left by its (effect-adjusted) speed-per-tick. */
    private void advancePosition(Zombie zombie, float dt) {
        zombie.setX(zombie.getX() - zombie.getEffectiveSpeedPerTick() * 900 * dt);
    }

    /** Returns the first ready skill (as the next state), else null. */
    private ZombieState checkForSkill(Zombie zombie, GameContext ctx, float dt) {
        for (ZombieState skill : zombie.getSkills()) {
            if (skill.shouldTrigger(zombie, ctx, dt)) {
                return skill;
            }
        }
        return null;
    }
}
