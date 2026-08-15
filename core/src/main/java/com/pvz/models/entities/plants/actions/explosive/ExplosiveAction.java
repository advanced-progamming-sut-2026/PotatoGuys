package com.pvz.models.entities.plants.actions.explosive;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.ExplosiveConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class ExplosiveAction extends PlantAction {
    ExplosiveConfig config;
    String currentClip;
    private boolean triggered;
    PamAnimationConfig pamAnimationConfig;

    public ExplosiveAction(ExplosiveConfig config) {
        this.config = config;
        triggered = false;
        currentClip = config.plantClip;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return true;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        pamAnimationConfig = plant.getSheet().pamAnimationConfig;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime >= config.plantTime && !triggered) {
            triggered = true;
            currentClip = config.idleClip;
            stateTime = 0;
        }
        if (stateTime >= config.idleTime && triggered) {
            if (plant.getType() == PlantType.CherryBomb ||
                plant.getType() == PlantType.Jalapeno ||
                plant.getType() == PlantType.Doomshroom) {
                List<Tile> targetTiles = new ArrayList<>();
                if (plant.getType() == PlantType.CherryBomb) {
                    for (int i = plant.getCol() - 1; i < plant.getCol() + 2; i++) {
                        for (int j = plant.getLane() - 1; j < plant.getLane() + 2; j++) {
                            Tile tile = ctx.getMap().getTileAt(i, j);
                            if (tile != null) targetTiles.add(tile);
                        }
                    }
                } else if (plant.getType() == PlantType.Jalapeno) {
                    for (int i = 0; i < ctx.getMap().getColumns(); i++) {
                        Tile tile = ctx.getMap().getTileAt(i, plant.getLane());
                        if (tile != null) targetTiles.add(tile);
                    }
                } else {
                    for (int i = plant.getCol() - 1; i < plant.getCol() + 2; i++) {
                        for (int j = plant.getLane() - 2; j <= plant.getLane() + 1; j++) {
                            Tile tile = ctx.getMap().getTileAt(i, j);
                            if (tile != null) targetTiles.add(tile);
                        }
                    }
                }
                plant.changeState(new ExplosionAction(config, null, targetTiles));
            } else {
                plant.changeState(new ExplosiveReadyAction(config));
            }
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position = new Vector2(GameController.colToWorldX(plant.getCol()), GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        return new FrameConfig(pamAnimationConfig.pamFilePath, currentClip, stateTime, position, scale, null, true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
