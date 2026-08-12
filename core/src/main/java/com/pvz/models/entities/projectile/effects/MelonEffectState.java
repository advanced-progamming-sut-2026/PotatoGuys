package com.pvz.models.entities.projectile.effects;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.games.GameContext;

/**
 * Melon-pult / Winter Melon-pult: area damage over the 3x3 tile block centered on the
 * landing point. When {@code chills} is true (Winter Melon), every zombie hit is also
 * chilled — same shared shape as {@link AreaFireEffectState}, parameterized so both
 * melon variants reuse one class instead of duplicating the area-lookup logic.
 */
public class MelonEffectState extends ProjectileEffectState {

    /** 100 ticks == 10 in-game seconds at Zombie.TICKS_PER_SECOND (10). */
    private static final int CHILL_TICKS = 100;

    private final boolean chills;

    /** @param chills true for Winter Melon-pult, false for plain Melon-pult */
    public MelonEffectState(boolean chills) {
        this.chills = chills;
    }

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        int centerCol = GameController.worldXtoCol(projectile.getX());
        int centerLane = GameController.worldYtoLane(projectile.getY());

        for (int dCol = -1; dCol <= 1; dCol++) {
            for (int dLane = -1; dLane <= 1; dLane++) {
                int col = centerCol + dCol;
                int lane = centerLane + dLane;
                if (col < 0 || lane < 0 || col >= ctx.getMap().getColumns() || lane >= ctx.getMap().getLanes()) {
                    continue;
                }
                for (Zombie zombie : ctx.getZombiesAt(col, lane)) {
                    zombie.takeDamage(projectile.getDamage(), false);
                    if (chills) {
                        zombie.applyEffect(new StatusEffect(EffectType.CHILL, CHILL_TICKS));
                    }
                }
            }
        }
    }

    @Override
    public String getLabel() {
        return chills ? "WinterMelon" : "Melon";
    }
}
