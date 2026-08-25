package com.pvz.models.entities.plants.actions;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.SunProducerActionConfig;
import com.pvz.models.entities.plants.data.ProductionKind;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.sun.SunType;
import com.pvz.models.games.GameContext;

/**
 * Config-driven sun production for {@code SUN_PRODUCER} plants, mirroring how
 * {@link ShooterAction}/{@link LobberAction} consume their config payload:
 *
 * <ul>
 *   <li>{@link ProductionKind#FIXED} — produces {@code amount} every interval (Sunflower, Primal Sunflower).</li>
 *   <li>{@link ProductionKind#STAGED} — amount scales with the plant's growth stage
 *       ({@code amounts[stage]} — Sun-shroom's 25/50/75).</li>
 *   <li>{@link ProductionKind#ONESHOT} — produces once and immediately dies (Gold Bloom).</li>
 * </ul>
 *
 * <p>Production is autonomous (no zombie precondition). The {@code Double Sun Chance}
 * upgrade flag, when unlocked, doubles the produced amount 50 % of the time.
 * {@link #produce} is static so Plant Food can reuse the exact same payload.
 */
public class SunProducerAction extends PlantAction {

    protected final SunProducerActionConfig config;
    private final boolean killOnOneShot;
    protected float animTime = 0f;
    protected boolean produced = false;

    public SunProducerAction(SunProducerActionConfig config) {
        this(config, true);
    }

    /** @param killOnOneShot whether an {@link ProductionKind#ONESHOT} run dies after producing
     *                       (attack behaviour). Plant Food reuses the same config but must survive. */
    public SunProducerAction(SunProducerActionConfig config, boolean killOnOneShot) {
        this.config = config;
        this.killOnOneShot = killOnOneShot;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;
        if (stateTime < config.intervalSeconds) {
            return false;
        }
        return true;
    }

    @Override
    public String getLabel() {
        return "ProduceSun";
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        produced = false;
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        animTime = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, config.label);
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (plant.isDead()) {
            return;
        }
        if (!produced && stateTime >= config.delaySeconds) {
            produced = true;
            produce(config, plant, ctx);
            if (config.productionKind == ProductionKind.ONESHOT && killOnOneShot) {
                plant.kill();
                return;
            }
        }
        if (stateTime >= animTime) {
            plant.changeState(new PlantIdleState());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);
        return new FrameConfig(pam.pamFilePath, config.label, stateTime, pos, scale, null, true);
    }

    /** Spawns one sun burst from {@code config}, honoring growth-stage amounts, the spawn
     *  offset and the Double Sun upgrade flag. Reused by Plant Food. */
    public static void produce(SunProducerActionConfig config, Plant plant, GameContext ctx) {
        float amount = resolveAmount(config, plant);
        if (amount <= 0f) {
            return;
        }

        if (plant.getUnlockedFlags().contains("Double Sun Chance") && Math.random() < 0.5) {
            amount *= 2f;
        }

        Sun sun = new Sun(SunType.NORMAL, plant.getCol(), plant.getLane(), (int) amount, false, ctx);
        sun.getCurrentPos().add(config.spawnOffset.x, config.spawnOffset.y);
        ctx.spawnSun(sun);
        ctx.log("[Action] " + plant.getSheet().getName() + " produced " + (int) amount + " sun.");
    }

    private static float resolveAmount(SunProducerActionConfig config, Plant plant) {
        if (config.productionKind == ProductionKind.STAGED && config.amounts != null && config.amounts.length > 0) {
            int stage = Math.max(0, Math.min(plant.getGrowthStageIndex(), config.amounts.length - 1));
            return config.amounts[stage];
        }
        return config.amount;
    }
}
