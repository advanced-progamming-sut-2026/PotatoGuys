package com.pvz.models.entities.plants.actions.shooters;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.CooldownPlantAction;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.models.games.GameContext;

public class ChargedShooterAction extends CooldownPlantAction {

    private final int requiredChargeTicks;
    private int currentChargeTicks;
    private boolean isFullyCharged;

    public ChargedShooterAction(float intervalSeconds, float chargeTimeSeconds) {
        super(intervalSeconds);
        this.requiredChargeTicks = (int) (chargeTimeSeconds * 30); // فرض بر ۳۰ تیک در ثانیه
        this.currentChargeTicks = 0;
        this.isFullyCharged = false;
    }

    @Override
    protected boolean canUse(Plant plant, GameContext ctx) {
        // ۱. پر شدن زمان شارژ
        if (!isFullyCharged) {
            currentChargeTicks++;
            if (currentChargeTicks >= requiredChargeTicks) {
                isFullyCharged = true;
                ctx.log("[Charge] " + plant.getSheet().getName() + " is fully charged!");
            }
            return false;
        }

        // ۲. چک کردن وجود زامبی در لاین پس از شارژ کامل
        return ctx.getZombiesInLane(plant.getLane()).stream()
                .anyMatch(z -> !z.isDead() && z.getX() >= plant.getCol());
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        if (!isFullyCharged) return;

        ProjectileType type = plant.getSheet().getProjectileType();
        float damage = plant.getEffectiveDamage();

        Projectile plasmaBall = new Projectile(ctx, type, plant.getLane(), plant.getCol(),
                                               damage, false, false, false, 0, null);

        // تنظیم جهت حرکت مستقیم به راست (dx=1, dy=0)
        plasmaBall.setVelocityVector(1.0f, 0.0f);
        ctx.spawnProjectile(plasmaBall);

        ctx.log("[Action] " + plant.getSheet().getName() + " fired a heavy charged ball!");

        // ریست کردن وضعیت شارژ
        isFullyCharged = false;
        currentChargeTicks = 0;
    }

    @Override
    public String getName() {
        return isFullyCharged ? "Charged-Shoot" : "Charging...";
    }
}
