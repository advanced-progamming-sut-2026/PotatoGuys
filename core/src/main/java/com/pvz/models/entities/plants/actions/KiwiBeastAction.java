package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.PhatBeetPulseEffect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.KiwiBeastConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class KiwiBeastAction extends PlantAction {

    private enum Phase { ATTACK, PF_GROWTH, PF_STRIKE }

    private final KiwiBeastConfig config;
    private final boolean feedMode;

    private int currentStage;
    private float growthAccumulator;
    private boolean playingGrowth;
    private float growthClipDuration;
    private String currentGrowthClip;

    private List<Zombie> targets;
    private boolean attacked;
    private float animDuration;
    private float cooldownTimer;

    private Phase phase;
    private float phaseTimer;
    private int pfHitIndex;
    private boolean[] pfHitDone;

    public KiwiBeastAction(KiwiBeastConfig config, boolean feedMode) {
        this.config = config;
        this.feedMode = feedMode;
    }

    private float currentDamage() {
        return config.baseDamage * resolveDamageMultiplier(currentStage);
    }

    private String currentAttackClip() {
        return resolveAttackLabel(currentStage);
    }

    private String currentIdleLabel() {
        return resolveIdleLabel(currentStage);
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        if (feedMode) return true;
        if (playingGrowth) return false;

        cooldownTimer += dt;
        growthAccumulator += dt;
        if (cooldownTimer < config.intervalSeconds) return false;

        int plantCol = plant.getCol();
        int plantLane = plant.getLane();
        float range = config.attackRangeFactor * GameMap.TILE_WIDTH;
        int radius = config.attackRadius;

        targets = new ArrayList<>();
        for (int lane = plantLane - radius; lane <= plantLane + radius; lane++) {
            for (int col = plantCol - radius; col <= plantCol + radius; col++) {
                for (Zombie z : ctx.getZombiesInLane(lane)) {
                    float dx = Math.abs(z.getX() - GameController.colToWorldX(col));
                    float dy = Math.abs(z.getY() - GameController.laneToWorldY(lane));
                    if (dx < range && dy < range) {
                        targets.add(z);
                    }
                }
            }
        }

        Tile frontTile = ctx.getMap().getTileAt(plantCol + 1, plantLane);
        boolean frontTileHasDestructible = frontTile != null
                && (frontTile.getTags().contains(TileTags.GRAVE) || frontTile.getTags().contains(TileTags.ICE_BLOCK));

        return !targets.isEmpty() || frontTileHasDestructible;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0;
        cooldownTimer = 0;
        attacked = false;

        if (feedMode) {
            onEnterFeed(plant, ctx);
        } else {
            onEnterAttack(plant, ctx);
        }
    }

    private void onEnterAttack(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        animDuration = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, currentAttackClip());
        if (animDuration <= 0) animDuration = config.intervalSeconds;
    }

    private void onEnterFeed(Plant plant, GameContext ctx) {
        phase = Phase.PF_GROWTH;
        phaseTimer = 0;
        pfHitIndex = 0;
        pfHitDone = new boolean[config.pfHitTimes.length];

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        growthClipDuration = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, config.pfGrowthClip);
        if (growthClipDuration <= 0) growthClipDuration = 0.5f;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);

        if (feedMode) {
            updateFeed(plant, ctx, dt);
        } else {
            updateAttack(plant, ctx, dt);
        }
    }

    private void updateAttack(Plant plant, GameContext ctx, float dt) {
        if (playingGrowth) {
            if (stateTime >= growthClipDuration) {
                playingGrowth = false;
                plant.changeState(new PlantIdleState());
            }
            return;
        }

        float hitTime = 0.53f;
        if (!attacked && stateTime > hitTime) {
            float damage = currentDamage();
            int radius = config.attackRadius;
            int plantCol = plant.getCol();
            int plantLane = plant.getLane();

            if (targets != null) {
                for (Zombie z : targets) {
                    z.takeDamage(damage);
                }
            }

            for (int lane = plantLane - radius; lane <= plantLane + radius; lane++) {
                for (int col = plantCol - radius; col <= plantCol + radius; col++) {
                    Tile tile = ctx.getMap().getTileAt(col, lane);
                    if (tile != null) tile.processHit(damage);
                }
            }

            Vector2 plantPos = new Vector2(
                    GameController.colToWorldX(plant.getCol()),
                    GameController.laneToWorldY(plant.getLane()));
            ctx.addEffect(new PhatBeetPulseEffect(ctx, plantPos,
                    config.effectPamPath, config.effectClip, config.effectScale));
            ctx.addEffect(new PhatBeetPulseEffect(ctx, plantPos,
                    config.tileHitPamPath, config.tileHitClip, config.tileHitScale));

            attacked = true;
        }

        if (stateTime >= animDuration) {
            checkAdvanceGrowth(plant, ctx);
        }
    }

    private void updateFeed(Plant plant, GameContext ctx, float dt) {
        phaseTimer += dt;

        switch (phase) {
            case PF_GROWTH:
                if (phaseTimer >= growthClipDuration) {
                    phase = Phase.PF_STRIKE;
                    phaseTimer = 0;
                }
                break;

            case PF_STRIKE:
                for (int i = pfHitIndex; i < config.pfHitTimes.length; i++) {
                    if (!pfHitDone[i] && phaseTimer >= config.pfHitTimes[i]) {
                        pfHitDone[i] = true;
                        pfHitIndex = i + 1;
                        dealPfDamage(plant, ctx);
                    }
                }

                float pfDuration = config.pfHitTimes[config.pfHitTimes.length - 1] + 0.5f;
                if (phaseTimer >= pfDuration) {
                    plant.changeState(new PlantIdleState());
                }
                break;
        }
    }

    private void dealPfDamage(Plant plant, GameContext ctx) {
        float damage = config.baseDamage * 3;
        int radius = config.pfRadius;
        int plantCol = plant.getCol();
        int plantLane = plant.getLane();

        for (int lane = plantLane - radius; lane <= plantLane + radius; lane++) {
            for (int col = plantCol - radius; col <= plantCol + radius; col++) {
                for (Zombie z : ctx.getZombiesAt(col, lane)) {
                    z.takeDamage(damage);
                }
                Tile tile = ctx.getMap().getTileAt(col, lane);
                if (tile != null) tile.processHit(damage);
            }
        }

        Vector2 plantPos = new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        ctx.addEffect(new PhatBeetPulseEffect(ctx, plantPos,
                config.pfEffectPamPath, config.pfEffectClip, config.effectScale));
    }

    private void checkAdvanceGrowth(Plant plant, GameContext ctx) {
        if (currentStage < config.stageIntervals.length
                && growthAccumulator >= config.stageIntervals[currentStage]) {
            int fromStage = currentStage;
            currentStage++;
            growthAccumulator = 0f;
            stateTime = 0f;

            String growthClip = resolveGrowthLabel(fromStage);
            if (growthClip != null) {
                PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
                growthClipDuration = AnimationCatalog.getInstance()
                        .getClipDuration(pam.pamFilePath, growthClip);
                currentGrowthClip = growthClip;
                playingGrowth = true;
            }
        } else {
            plant.changeState(new PlantIdleState());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        targets = null;
        attacked = false;
        phase = null;
        phaseTimer = 0;
        pfHitIndex = 0;
        pfHitDone = null;
        stateTime = 0;
    }

    private float resolveDamageMultiplier(int stage) {
        if (config.damageMultipliers != null && stage < config.damageMultipliers.length) {
            return config.damageMultipliers[stage];
        }
        return 1f;
    }

    private String resolveIdleLabel(int stage) {
        if (config.idleLabels != null && stage < config.idleLabels.length) {
            return config.idleLabels[stage];
        }
        return config.label;
    }

    private String resolveAttackLabel(int stage) {
        if (config.attackLabels != null && stage < config.attackLabels.length) {
            return config.attackLabels[stage];
        }
        return config.label;
    }

    private String resolveGrowthLabel(int fromStage) {
        if (config.growthLabels != null && fromStage < config.growthLabels.length) {
            return config.growthLabels[fromStage];
        }
        return null;
    }

    public String getCurrentIdleLabel() {
        return currentIdleLabel();
    }

    public boolean isPlayingGrowth() {
        return playingGrowth;
    }

    public String getCurrentGrowthClip() {
        return currentGrowthClip;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position = new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;

        String clip;
        boolean looping;
        float animTime;

        if (feedMode) {
            switch (phase) {
                case PF_GROWTH:
                    clip = config.pfGrowthClip;
                    looping = false;
                    animTime = phaseTimer;
                    break;
                case PF_STRIKE:
                    clip = config.pfAttackClip;
                    looping = true;
                    animTime = phaseTimer;
                    break;
                default:
                    clip = config.pfGrowthClip;
                    looping = false;
                    animTime = phaseTimer;
                    break;
            }
        } else if (playingGrowth) {
            clip = currentGrowthClip;
            looping = false;
            animTime = stateTime;
        } else {
            clip = currentAttackClip();
            looping = false;
            animTime = stateTime;
        }

        return new FrameConfig(pam.pamFilePath, clip, animTime, position, scale, null, looping);
    }

    @Override
    public String getLabel() {
        if (feedMode) return "KiwiBeastFeed";
        if (playingGrowth) return "KiwiBeastGrowth[stage" + (currentStage + 1) + "]";
        return "KiwiBeast[stage" + (currentStage + 1) + "]";
    }
}
