package com.pvz.models.entities.plants.actions.shooters;

import java.util.List;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.CooldownPlantAction;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.models.games.GameContext;

public class CyclingShooterAction extends CooldownPlantAction {

    private final List<ProjectileType> cycleTypes;
    private final List<Float> cycleDamages;
    private int currentIndex = 0;

    public CyclingShooterAction(float intervalSeconds, List<ProjectileType> types, List<Float> damages) {
        super(intervalSeconds);
        this.cycleTypes = types;
        this.cycleDamages = damages;
    }

    @Override
    protected boolean canUse(Plant plant, GameContext ctx) {
        return ctx.getZombiesInLane(plant.getLane()).stream()
                .anyMatch(z -> !z.isDead() && z.getX() >= plant.getCol());
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        if (cycleTypes == null || cycleTypes.isEmpty()) return;

        ProjectileType currentType = cycleTypes.get(currentIndex);
        float damage = cycleDamages.get(currentIndex);

        Projectile bulb = new Projectile(ctx, currentType, plant.getLane(), plant.getCol(),
                                         damage, false, false, false, 0, null);

        // حرکت اولیه به سمت راست
        bulb.setVelocityVector(1.0f, 0.0f);

        // فعال‌سازی قابلیت کمانه‌کردن دوبعدی روی زامبی‌ها
        bulb.setBouncing(true);

        ctx.spawnProjectile(bulb);
        ctx.log("[Action] " + plant.getSheet().getName() + " rolled bulb index: " + currentIndex);

        // چرخش به نوع پرتابه بعدی
        currentIndex = (currentIndex + 1) % cycleTypes.size();
    }

    @Override
    public String getName() {
        return "Cycling-Shoot";
    }
}
