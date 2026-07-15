package pvz.Models.Entities.Zombies.fsm;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.skills.ZombieSkill;
import pvz.Models.Games.GameContext;

/**
 * Generic skill-execution state.
 *
 * <p>The walk loop transitions here when a {@link ZombieSkill#shouldTrigger}
 * returns {@code true}. On {@link #onEnter} the skill fires its effect.
 * The state then immediately requests a transition back to {@link WalkState}
 * on the very next tick — the zombie is stationary for exactly one tick
 * (which is fine for instantaneous actions like stealing sun or raising a tomb).
 *
 * <p>For skills that need a longer animation pause (e.g. Gargantuar smash),
 * extend this class and override {@link #tick} to count down before returning
 * {@link WalkState}.
 */
public class SpecialActionState implements ZombieState {

    private final ZombieSkill skill;
    private int pauseTicksRemaining;

    /**
     * @param skill         the skill to execute on enter
     * @param pauseTicks    extra ticks to remain in this state after the skill fires
     *                      (use 0 for instant skills like Ra's sun-steal)
     */
    public SpecialActionState(ZombieSkill skill, int pauseTicks) {
        this.skill = skill;
        this.pauseTicksRemaining = pauseTicks;
    }

    /** Convenience constructor with zero pause (instant skill). */
    public SpecialActionState(ZombieSkill skill) {
        this(skill, 0);
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        skill.execute(zombie, ctx);
    }

    @Override
    public ZombieState tick(Zombie zombie, GameContext ctx) {
        if (pauseTicksRemaining > 0) {
            pauseTicksRemaining--;
            return this;
        }
        return new WalkState();
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Action[" + skill.getName() + "]";
    }
}
