package com.pvz.models.entities.zombies.skills;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.config.GargantuarSkillConfig;
import com.pvz.models.games.GameContext;

/**
 * Gargantuar — throws an Imp when the zombie drops to {@code throwHpFraction} % HP.
 *
 * <p>From JSON {@code ZombieGargantuarProps}:
 * <ul>
 *   <li>{@code HealthPercentThrowImp = 0.5}</li>
 *   <li>{@code ImpType = "ZombieTutorialImpDefault"} → resolved to a zombie alias by the registry</li>
 * </ul>
 *
 * <p>The skill fires its throw on state entry, then holds the "smash_left"
 * clip until it finishes. The Gargantuar also smashes plants it walks into
 * instead of eating them — that part lives in {@code EatState} via the
 * smashDamage stat.
 */
public class GargantuarSkill extends ZombieSkill {

    /** Imp lands 2 columns from the left edge. */
    private static final int IMP_TARGET_COL = 3;

    private final String impAlias;
    private final float throwHpFraction;

    public GargantuarSkill(GargantuarSkillConfig config) {
        super(config);
        this.impAlias = config.impType;
        this.throwHpFraction = config.throwHpFraction;
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        if (zombie.isImpAlreadyThrown()) return false;
        if (zombie.getMaxHp() <= 0f) return false;
        float hpFraction = zombie.getHp() / zombie.getMaxHp();
        return hpFraction <= throwHpFraction;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        zombie.markImpThrown();
        int targetCol = Math.max(1, Math.min((int)zombie.getX() - IMP_TARGET_COL, ctx.getMap().getColumns() - 1));
        Zombie imp = new ZombieFactory().create(impAlias, zombie.getX(), (int)zombie.getY(), ctx, 1, 2);
        ctx.spawnZombie(imp);
        ctx.log("Gargantuar threw Imp [" + impAlias + "] to column "
                + targetCol + " in lane " + GameController.worldYtoLane(zombie.getY()) + "!");
    }

    @Override
    public String getName() {
        return "ThrowImp[" + impAlias + "]";
    }
}
