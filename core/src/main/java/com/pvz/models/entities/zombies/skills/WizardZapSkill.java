package com.pvz.models.entities.zombies.skills;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.WizardZapSkillConfig;
import com.pvz.models.games.GameContext;

/**
 * Dark Ages Wizard Zombie — transforms the nearest plant into a harmless cat.
 *
 * <p>The wizard does <em>not</em> eat the plant it reaches; it transforms it
 * instead, which is reflected in {@code EatState}. Casts every
 * {@code cooldownSeconds}.
 */
public class WizardZapSkill extends CooldownSkill {

    public WizardZapSkill(WizardZapSkillConfig config) {
        super(config, config.cooldownSeconds);
    }

    @Override
    protected boolean canUse(Zombie zombie, GameContext ctx) {
        return findTargetCol(zombie, ctx) >= 0;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        int col = GameController.worldXtoCol(zombie.getX());
        for (int c = col; c >= 0; c--) {
            if (ctx.isPlantAt(c, GameController.worldYtoLane(zombie.getY()))) {
                // ctx.transformPlantToCat(c, zombie.getLane());
                ctx.log("Wizard Zombie cast a spell! Plant at ("
                        + c + "," + GameController.worldYtoLane(zombie.getY()) + ") is now a cat.");
                return;
            }
        }
    }

    @Override
    public String getName() {
        return "WizardZap";
    }

    private int findTargetCol(Zombie zombie, GameContext ctx) {
        int col = GameController.worldXtoCol(zombie.getX());
        for (int c = col; c >= 0; c--) {
            if (ctx.isPlantAt(c, GameController.worldYtoLane(zombie.getY()))) return c;
        }
        return -1;
    }
}
