package com.pvz.models.entities.plants.actions;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.SunBeanConfig;
import com.pvz.models.entities.plants.config.SunProducerActionConfig;
import com.pvz.models.entities.plants.data.ProductionKind;
import com.pvz.models.games.GameContext;

/**
 * Dedicated behaviour action for the Sun Bean plant.
 *
 * <p>It behaves like a wall-nut (swapping idle / damage clips as it loses HP)
 * and drops one sun onto its own tile on every zombie bite.
 *
 * <p>Sun production is triggered directly from {@link Plant#takeDamage} via
 * {@link #spawnHitSun} — the exact moment a bite lands — rather than by scanning
 * for eaters in the FSM loop. This is reliable even while the plant is wrapped in
 * a damage-flash state or guarded by a shield.
 *
 * <p>The dropped sun is created through the same shared
 * {@link SunProducerAction#produce} path that Sunflowers use, so it appears
 * sitting on the plant's tile, is rendered on top of the plant, and can be
 * clicked to collect — exactly like a {@code SUN_PRODUCER} plant.
 */
public class SunBeanAction extends PlantAction {

    private final SunBeanConfig config;
    private final SunProducerActionConfig produceConfig;
    private String currentClip;

    public SunBeanAction(SunBeanConfig config) {
        this.config = config;
        this.currentClip = config.idleClip;
        this.produceConfig = new SunProducerActionConfig();
        this.produceConfig.productionKind = ProductionKind.FIXED;
        this.produceConfig.amount = config.sunOnHit;
        this.produceConfig.spawnOffset = config.spawnOffset;
        this.produceConfig.delaySeconds = 0f;
    }

    /** Produces one sun on the plant's tile. Called once per landed bite. */
    public void spawnHitSun(Plant plant, GameContext ctx) {
        int sunAmount = effectiveSunOnHit(plant);
        if (sunAmount <= 0 || plant.isDead()) {
            return;
        }
        produceConfig.amount = sunAmount;
        SunProducerAction.produce(produceConfig, plant, ctx);
        ctx.log("[SunBean] " + plant.getSheet().getName() + " dropped " + sunAmount + " sun.");
    }

    /** Base sun-per-bite + the "Sun Drop +5" level flag. */
    private int effectiveSunOnHit(Plant plant) {
        return config.sunOnHit + (plant.getUnlockedFlags().contains("Sun Drop +5") ? 5 : 0);
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return true;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        currentClip = config.idleClip;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        updateClip(plant);
    }

    private void updateClip(Plant plant) {
        float maxHp = plant.getMaxHp();
        float fraction = maxHp <= 0f ? 1f : plant.getHp() / maxHp;
        if (fraction <= 0.25f) {
            currentClip = config.damage3Clip;
        } else if (fraction <= 0.5f) {
            currentClip = config.damage2Clip;
        } else if (fraction <= 0.75f) {
            currentClip = config.damage1Clip;
        } else {
            currentClip = config.idleClip;
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        Vector2 pos = new Vector2(GameController.colToWorldX(plant.getCol()), GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        return new FrameConfig(pam.pamFilePath, currentClip, stateTime, pos, scale, plant.getArmorPartsVisibility(), true);
    }

    @Override
    public String getLabel() {
        return "SunBean[" + currentClip + "]";
    }
}
