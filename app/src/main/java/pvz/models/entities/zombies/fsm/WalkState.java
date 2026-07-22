package pvz.models.entities.zombies.fsm;

import pvz.models.entities.zombies.Zombie;
import pvz.models.entities.zombies.skills.ZombieSkill;
import pvz.models.games.GameContext;

/**
 * Default movement state — the zombie walks left across the lawn.
 *
 * <p>Each tick the zombie advances by its effective speed (accounting for
 * chill/slow effects). After moving, three checks are evaluated in order:
 *
 * <ol>
 *   <li><b>Boundary check</b> — if {@code x ≤ 0} trigger the lawn-mower.</li>
 *   <li><b>Plant check</b> — if a plant occupies the zombie's current cell,
 *       transition to {@link EatState}.</li>
 *   <li><b>Skill check</b> — iterate the zombie's skill list; if any skill's
 *       {@link ZombieSkill#shouldTrigger} is {@code true}, transition to
 *       {@link SpecialActionState}.</li>
 * </ol>
 */
public class WalkState implements ZombieState {

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        // no visual cue needed for walking
    }

    @Override
    public ZombieState tick(Zombie zombie, GameContext ctx) {
        advancePosition(zombie);

        ZombieState eatTransition = checkForPlant(zombie, ctx);
        if (eatTransition != null) return eatTransition;

        ZombieState skillTransition = checkForSkill(zombie, ctx);
        if (skillTransition != null) return skillTransition;

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

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Move the zombie left by its (effect-adjusted) speed-per-tick. */
    private void advancePosition(Zombie zombie) {
        zombie.setX(zombie.getX() - zombie.getEffectiveSpeedPerTick());
    }

    /** Returns {@link EatState} if a plant is at the zombie's current column, else null. */
    private ZombieState checkForPlant(Zombie zombie, GameContext ctx) {
        int col = (int) zombie.getX();
        if (ctx.isPlantAt(col, zombie.getLane()) && !ctx.getPlantsAt(col,zombie.getLane()).getLast().isFrozen()) {
            return new EatState(col, zombie.getLane());
        }
        return null;
    }

    /** Returns {@link SpecialActionState} for the first ready skill, else null. */
    private ZombieState checkForSkill(Zombie zombie, GameContext ctx) {
        for (ZombieSkill skill : zombie.getSkills()) {
            if (skill.shouldTrigger(zombie, ctx)) {
                return new SpecialActionState(skill);
            }
        }
        return null;
    }
}
