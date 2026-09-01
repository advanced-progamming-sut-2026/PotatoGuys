package com.pvz.models.entities.plants.actions;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.MintActionConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.games.GameContext;

public class MintAction extends PlantAction {

    private static final int PHASE_INTRO = 0;
    private static final int PHASE_LOOP  = 1;
    private static final int PHASE_OUTRO = 2;

    private int phase;
    private float introDuration;
    private float loopDuration;
    private float outroDuration;
    private float phaseTime;
    private boolean buffApplied;

    public MintAction(MintActionConfig config) {
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return true;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        phase = PHASE_INTRO;
        phaseTime = 0f;
        buffApplied = false;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        introDuration = Math.max(0.1f, catalog.getClipDuration(pam.pamFilePath, "intro"));
        loopDuration  = Math.max(0.1f, catalog.getClipDuration(pam.pamFilePath, "loop"));
        outroDuration = Math.max(0.1f, catalog.getClipDuration(pam.pamFilePath, "outro"));
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (plant.isDead()) return;

        phaseTime += dt;

        switch (phase) {
            case PHASE_INTRO:
                if (phaseTime >= introDuration) {
                    phase = PHASE_LOOP;
                    phaseTime = 0f;
                    if (!buffApplied) {
                        buffApplied = true;
                        applyFamilyBuff(plant, ctx);
                    }
                }
                break;
            case PHASE_LOOP:
                if (phaseTime >= loopDuration) {
                    phase = PHASE_OUTRO;
                    phaseTime = 0f;
                }
                break;
            case PHASE_OUTRO:
                if (phaseTime >= outroDuration) {
                    plant.kill();
                }
                break;
        }
    }

    private void applyFamilyBuff(Plant mint, GameContext ctx) {
        var family = mint.getSheet().getCategory();
        int buffed = 0;
        for (Plant member : List.copyOf(ctx.getPlants())) {
            if (member == mint || member.isDead()) continue;
            if (member.getSheet().getCategory() == family) {
                member.triggerPlantFood(ctx);
                buffed++;
            }
        }
        ctx.log("[Mint] " + mint.getSheet().getName()
                + " applied Plant Food to " + buffed
                + " " + family + " plant(s).");
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

        String clip;
        boolean looping;
        switch (phase) {
            case PHASE_LOOP:
                clip = "loop";
                looping = true;
                break;
            case PHASE_OUTRO:
                clip = "outro";
                looping = false;
                break;
            default:
                clip = "intro";
                looping = false;
                break;
        }
        return new FrameConfig(pam.pamFilePath, clip, phaseTime, pos, scale, null, looping);
    }

    @Override
    public String getLabel() {
        return "Mint";
    }
}
