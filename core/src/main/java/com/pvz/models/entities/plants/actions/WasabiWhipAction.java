package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.WasabiWhipConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;

public class WasabiWhipAction extends PlantAction {

    private enum Phase { ATTACK, PF_ON, PF_STRIKE, PF_OFF }

    private final WasabiWhipConfig config;
    private final boolean feedMode;
    private boolean hasFront;
    private boolean hasBack;
    private List<Zombie> frontTargets;
    private List<Zombie> backTargets;
    private boolean attacked;
    private Phase phase;
    private float phaseTimer;
    private float animDuration;

    public WasabiWhipAction(WasabiWhipConfig config, boolean feedMode) {
        this.config = config;
        this.feedMode = feedMode;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        if (feedMode) return true;

        int plantCol = plant.getCol();
        int plantLane = plant.getLane();
        float range = config.attackRangeFactor * GameMap.TILE_WIDTH;

        frontTargets = new ArrayList<>();
        backTargets = new ArrayList<>();

        for (Zombie z : ctx.getZombiesInLane(plantLane)) {
            float dx = z.getX() - GameController.colToWorldX(plantCol);
            float dy = Math.abs(z.getY() - GameController.laneToWorldY(plantLane));
            if (dy >= range) continue;

            if (dx > 0 && dx < range) {
                frontTargets.add(z);
            } else if (dx < 0 && -dx < range) {
                backTargets.add(z);
            }
        }

        hasFront = !frontTargets.isEmpty();
        hasBack = !backTargets.isEmpty();

        return hasFront || hasBack;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0;
        phaseTimer = 0;
        attacked = false;

        if (feedMode) {
            phase = Phase.PF_ON;
            PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
            String pamPath = pam.pamFilePath;
            float onDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.pfOnClip);
            float offDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.pfOffClip);
            if (onDuration <= 0) onDuration = 0.3f;
            if (offDuration <= 0) offDuration = 0.3f;
            animDuration = onDuration + config.pfDuration + offDuration;
        } else {
            phase = Phase.ATTACK;
            PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
            String pamPath = pam.pamFilePath;
            String clip = currentAttackClip();
            animDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, clip);
            if (animDuration <= 0) animDuration = config.intervalSeconds;
        }
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        phaseTimer += dt;

        switch (phase) {
            case ATTACK:
                if (!attacked && stateTime > animDuration * 0.4f) {
                    float damage = config.baseDamage;
                    for (Zombie z : frontTargets) {
                        z.takeDamage(damage);
                    }
                    for (Zombie z : backTargets) {
                        z.takeDamage(damage);
                    }

                    int plantCol = plant.getCol();
                    int plantLane = plant.getLane();
                    Tile frontTile = ctx.getMap().getTileAt(plantCol + 1, plantLane);
                    if (frontTile != null) frontTile.processHit(damage);
                    Tile backTile = ctx.getMap().getTileAt(plantCol - 1, plantLane);
                    if (backTile != null) backTile.processHit(damage);

                    attacked = true;
                }
                if (stateTime >= animDuration) {
                    plant.changeState(new PlantIdleState());
                }
                break;

            case PF_ON:
                if (phaseTimer >= AnimationCatalog.getInstance().getClipDuration(
                        plant.getSheet().pamAnimationConfig.pamFilePath, config.pfOnClip)) {
                    phase = Phase.PF_STRIKE;
                    phaseTimer = 0;
                }
                break;

            case PF_STRIKE:
                if (!attacked && phaseTimer > 0.1f) {
                    int plantCol = plant.getCol();
                    int plantLane = plant.getLane();
                    int radius = config.pfRadius;

                    for (int lane = plantLane - radius; lane <= plantLane + radius; lane++) {
                        for (int col = plantCol - radius; col <= plantCol + radius; col++) {
                            for (Zombie z : ctx.getZombiesAt(col, lane)) {
                                z.takeDamage(config.baseDamage);
                            }
                            Tile tile = ctx.getMap().getTileAt(col, lane);
                            if (tile != null) tile.processHit(config.baseDamage);
                        }
                    }
                    attacked = true;
                }
                if (phaseTimer >= config.pfDuration) {
                    phase = Phase.PF_OFF;
                    phaseTimer = 0;
                }
                break;

            case PF_OFF:
                float offDuration = AnimationCatalog.getInstance().getClipDuration(
                        plant.getSheet().pamAnimationConfig.pamFilePath, config.pfOffClip);
                if (offDuration <= 0) offDuration = 0.3f;
                if (phaseTimer >= offDuration) {
                    plant.changeState(new PlantIdleState());
                }
                break;
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        frontTargets = null;
        backTargets = null;
        hasFront = false;
        hasBack = false;
        attacked = false;
        phase = null;
        phaseTimer = 0;
        stateTime = 0;
    }

    private String currentAttackClip() {
        if (hasFront && hasBack) return config.attackBothClip;
        if (hasFront) return config.attackFrontClip;
        return config.attackBackClip;
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
                case PF_ON:
                    clip = config.pfOnClip;
                    looping = false;
                    animTime = phaseTimer;
                    break;
                case PF_STRIKE:
                    clip = config.pfClip;
                    looping = true;
                    animTime = phaseTimer;
                    break;
                case PF_OFF:
                    clip = config.pfOffClip;
                    looping = false;
                    animTime = phaseTimer;
                    break;
                default:
                    clip = config.pfOnClip;
                    looping = false;
                    animTime = phaseTimer;
                    break;
            }
        } else {
            clip = currentAttackClip();
            looping = false;
            animTime = stateTime;
        }

        return new FrameConfig(pam.pamFilePath, clip, animTime, position, scale, null, looping);
    }

    @Override
    public String getLabel() {
        if (feedMode) return "WasabiWhipFeed[" + (phase != null ? phase.name() : "?") + "]";
        return "WasabiWhip[" + currentAttackClip() + "]";
    }
}
