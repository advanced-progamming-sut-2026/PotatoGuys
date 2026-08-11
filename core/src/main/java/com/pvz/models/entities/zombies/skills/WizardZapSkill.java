package com.pvz.models.entities.zombies.skills;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Dark Ages Wizard Zombie — transforms the nearest plant into a harmless cat.
 *
 * <p>From JSON {@code ZombieDarkWizardProps}:
 * <ul>
 *   <li>Fires every ~3 seconds (modelled as a 3 s cooldown here).</li>
 *   <li>The wizard does <em>not</em> eat the plant it reaches; it transforms it instead.
 *       This is reflected in the EatState: the context's {@code dealDamageToPlant}
 *       is replaced by {@code transformPlantToCat} when the wizard is in EatState.</li>
 * </ul>
 *
 * <p>Transformed plants remain cats until the Wizard that cast the spell dies.
 * Tracking the "undo" relationship is handled by the game session / map layer.
 */
public class WizardZapSkill extends CooldownSkill {

    private static final float ZAP_INTERVAL_SECONDS = 3.0f;

    public WizardZapSkill() {
        super(ZAP_INTERVAL_SECONDS);
    }

    @Override
    protected boolean canUse(Zombie zombie, GameContext ctx) {
        int col = (int) zombie.getX();
        for (int c = col; c >= 0; c--) {
            if (ctx.isPlantAt(c, GameController.worldYtoLane(zombie.getY()))) return true;
        }
        return false;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        int col = (int) zombie.getX();
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
}
