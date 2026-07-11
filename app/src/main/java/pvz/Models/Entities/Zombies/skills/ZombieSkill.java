package pvz.Models.Entities.Zombies.skills;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieGameContext;

/**
 * A discrete, re-usable special ability plug-in.
 *
 * <p>Skills are attached to a {@link Zombie} instance by the {@link
 * pvz.Models.Entities.Zombies.ZombieFactory}. On every tick inside
 * {@link pvz.Models.Entities.Zombies.fsm.WalkState}, the zombie iterates
 * its skill list and transitions to {@link
 * pvz.Models.Entities.Zombies.fsm.SpecialActionState} as soon as any skill
 * reports {@code shouldTrigger == true}.
 *
 * <p><b>Design note:</b> Skills are stateful (they track cooldowns, ammo, etc.)
 * but are owned by the specific zombie instance, so no thread-safety concerns arise.
 */
public interface ZombieSkill {

    /**
     * Evaluated every tick by the walk loop.
     * Should be cheap — avoid heavy computation; rely on cached/incremented counters.
     *
     * @return true if the skill is ready and conditions for its use are met
     */
    boolean shouldTrigger(Zombie zombie, ZombieGameContext ctx);

    /**
     * Performs the skill's game-world effect.
     * Called once by {@link pvz.Models.Entities.Zombies.fsm.SpecialActionState#onEnter}.
     */
    void execute(Zombie zombie, ZombieGameContext ctx);

    /** Short label shown in CLI debug output, e.g. {@code "StealSun"}, {@code "RaiseTomb"}. */
    String getName();
}
