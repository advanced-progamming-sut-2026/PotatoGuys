package com.pvz.models.entities.zombies.skills;

import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.games.GameContext;

/**
 * Gargantuar — throws an Imp when the zombie drops to 50 % HP.
 *
 * <p>From JSON {@code ZombieGargantuarProps}:
 * <ul>
 *   <li>{@code HealthPercentThrowImp = 0.5}</li>
 *   <li>{@code ImpTargetColumn = 2} — imp lands at column index 2 (0-based from left)</li>
 *   <li>{@code ImpType = "egypt_imp"} → resolved to a zombie alias by the registry</li>
 * </ul>
 *
 * <p>The Gargantuar also smashes plants it walks into instead of eating them.
 * That is modelled inside {@link pvz.models.entities.zombies.fsm.EatState}
 * by setting the eat-DPS to {@code smashDamage} in one hit.
 */
public class GargantuarSkill implements ZombieSkill {

    /** JSON ImpTargetColumn: imp lands 2 columns from the left edge. */
    private static final int IMP_TARGET_COL = 3;

    private final String impAlias;

    /** HP fraction at which the imp is thrown (from JSON HealthPercentThrowImp). */
    private final float throwHpFraction;

    /**
     * @param impAlias       full zombie registry alias for the imp to spawn
     * @param throwHpFraction fraction of max HP (0..1) that triggers the throw
     */
    public GargantuarSkill(String impAlias, float throwHpFraction) {
        this.impAlias = impAlias;
        this.throwHpFraction = throwHpFraction;
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx) {
        if (zombie.isImpAlreadyThrown()) return false;
        if (zombie.getMaxHp() <= 0f) return false;
        float hpFraction = zombie.getHp() / zombie.getMaxHp();
        return hpFraction <= throwHpFraction;
    }

    @Override
    public void execute(Zombie zombie, GameContext ctx) {
        zombie.markImpThrown();
        int targetCol = Math.max(1, Math.min((int)zombie.getX() - IMP_TARGET_COL, ctx.getColumns() - 1));
        Zombie imp = new ZombieFactory().create(impAlias, targetCol, zombie.getLane(), ctx, 1, 2);
        ctx.spawnZombie(imp);
        ctx.log("Gargantuar threw Imp [" + impAlias + "] to column "
                + targetCol + " in lane " + zombie.getLane() + "!");
    }

    @Override
    public String getName() {
        return "ThrowImp[" + impAlias + "]";
    }
}
