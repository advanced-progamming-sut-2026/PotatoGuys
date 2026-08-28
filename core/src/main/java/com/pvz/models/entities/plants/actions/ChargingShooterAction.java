package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.ChargingShooterConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.TileTags;

/**
 * A {@link ShooterAction} variant for plants that must charge up before they can
 * fire (e.g. Citron).
 *
 * <p>Behaviour:
 * <ul>
 *   <li>After being planted, plays a charge animation for {@code chargeDuration}
 *       seconds.  During this phase the plant cannot fire.</li>
 *   <li>Once charged, enters a ready-idle and waits for a zombie to appear in
 *       its lane.</li>
 *   <li>On detection, plays the attack clip and fires projectile patterns.</li>
 *   <li>The first shot after charging fires immediately (no cooldown).</li>
 * </ul>
 */
public class ChargingShooterAction extends ShooterAction {

    private final ChargingShooterConfig chargeConfig;
    private boolean charged = false;
    private float chargeTime = 0f;
    private boolean firstShot = true;

    public ChargingShooterAction(ChargingShooterConfig config) {
        super(config);
        this.chargeConfig = config;
    }

    // ── Query helpers for PlantIdleState ─────────────────────────────────────

    /** {@code true} while the charge animation is still playing. */
    public boolean isCharging() {
        return !charged;
    }

    /** Returns the PAM clip label for the charge animation. */
    public String getChargeLabel() {
        return chargeConfig.chargeLabel;
    }

    /** Returns the idle clip label used after charging is complete. */
    public String getReadyIdleLabel() {
        return chargeConfig.idleLabel;
    }

    /** Whether the idle animation should loop (false during charge, true after). */
    public boolean shouldLoopIdle() {
        return charged;
    }

    // ── Trigger logic ────────────────────────────────────────────────────────

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;

        // ── Phase 1: charging ──
        if (!charged) {
            chargeTime += dt;
            if (chargeTime >= chargeConfig.chargeDuration) {
                charged = true;
                stateTime = 0f;          // reset so interval check starts fresh
            }
            return false;
        }

        // ── Phase 2: ready — first shot fires as soon as a zombie appears ──
        if (firstShot) {
            return laneHasTarget(plant, ctx);
        }

        // ── Phase 3: subsequent shots respect the cooldown interval ──
        if (stateTime < chargeConfig.intervalSeconds) {
            return false;
        }
        return laneHasTarget(plant, ctx);
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        super.onEnter(plant, ctx);
        firstShot = false;
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        charged = false;
        chargeTime = 0f;
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);
        return new FrameConfig(pam.pamFilePath, chargeConfig.label, stateTime, pos, scale, null, true);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean laneHasTarget(Plant plant, GameContext ctx) {
        boolean hasZombie = ctx.getZombiesInLane(plant.getLane()).stream()
                .anyMatch(z -> z.getX() >= plant.getCol());
        if (hasZombie) return true;

        for (int i = plant.getCol(); i < ctx.getMap().getColumns(); i++) {
            List<TileTags> tags = ctx.getTileAt(i, plant.getLane()).getTags();
            if (tags.contains(TileTags.GRAVE) || tags.contains(TileTags.ICE_BLOCK)) {
                return true;
            }
        }
        return false;
    }
}
