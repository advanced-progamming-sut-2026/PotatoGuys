package com.pvz.models.entities.plants.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.ChargingShooterAction;
import com.pvz.models.entities.plants.actions.ClipProgressionShooterAction;
import com.pvz.models.entities.plants.actions.GrowthMeleeAction;
import com.pvz.models.entities.plants.actions.GrowthSunProducerAction;
import com.pvz.models.entities.plants.actions.KiwiBeastAction;
import com.pvz.models.entities.plants.actions.MultiStageFeedAction;
import com.pvz.models.entities.plants.actions.MultiStageShooterAction;
import com.pvz.models.entities.plants.actions.StackedShooterAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.games.GameContext;

public class PlantIdleState extends PlantState {

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        // no visual cue needed for idling
    }

    @Override
    public void update(Plant plant, GameContext ctx , float dt) {
        super.update(plant, ctx, dt);
        if (plant.getAttackAction().shouldTrigger(plant, ctx , dt)) {
            plant.changeState(plant.getAttackAction());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Idle";
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x,y);
        Vector2 scale = new Vector2(0.7f,0.7f);

        String idleLabel = config.idleLabel;
        boolean looping = true;
        float animTime = stateTime;
        if (plant.getAttackAction() instanceof GrowthSunProducerAction growthAction) {
            idleLabel = growthAction.getIdleLabel(plant);
        } else if (plant.getAttackAction() instanceof GrowthMeleeAction growthMeleeAction) {
            idleLabel = growthMeleeAction.getCurrentIdleLabel();
        } else if (plant.getAttackAction() instanceof KiwiBeastAction kiwiAction) {
            idleLabel = kiwiAction.getCurrentIdleLabel();
        } else if (plant.getAttackAction() instanceof StackedShooterAction stackedAction) {
            idleLabel = stackedAction.getIdleLabel();
        } else if (plant.getAttackAction() instanceof ChargingShooterAction chargingAction) {
            if (chargingAction.isCharging()) {
                idleLabel = chargingAction.getChargeLabel();
                looping = false;
            } else {
                idleLabel = chargingAction.getReadyIdleLabel();
            }
        } else if (plant.getAttackAction() instanceof ClipProgressionShooterAction cpAction) {
            idleLabel = cpAction.getCurrentIdleLabel();
            looping = cpAction.shouldLoopIdle();
            if (!looping) {
                animTime = cpAction.getClipElapsedTime();
            }
        } else if (plant.getAttackAction() instanceof MultiStageShooterAction msAction) {
            idleLabel = msAction.getCurrentIdleLabel();
            looping = msAction.shouldLoopIdle();
            if (!looping) {
                animTime = msAction.getClipElapsedTime();
            }
        } else if (plant.getAttackAction() instanceof MultiStageFeedAction mfAction) {
            idleLabel = mfAction.getCurrentIdleLabel();
            looping = mfAction.shouldLoopIdle();
            animTime = mfAction.getClipElapsedTime();
        }

        return new FrameConfig(config.pamFilePath, idleLabel, animTime, pos, scale, null, looping);
    }
}
