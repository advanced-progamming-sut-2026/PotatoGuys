package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.BonkChoyConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class BonkChoyAttack extends PlantAction{
    public List<Zombie> targets;
    private boolean attacked;
    private BonkChoyConfig bonkChoyConfig;

    public BonkChoyAttack(BonkChoyConfig bonkChoyConfig){
        this.bonkChoyConfig=bonkChoyConfig;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        int plantCol = plant.getCol();
        int plantLane = plant.getLane();
        float range = bonkChoyConfig.attackRangeFactor * GameMap.TILE_WIDTH;

        targets = new ArrayList<>();
        for (int lane = plantLane - 1; lane <= plantLane + 1; lane++) {
            for (int col = plantCol - 1; col <= plantCol + 1; col++) {
                if (lane == plantLane && col == plantCol) continue;
                for (Zombie z : ctx.getZombiesInLane(lane)) {
                    float zx = z.getX();
                    float zy = z.getY();
                    float tileX = GameController.colToWorldX(col);
                    float tileY = GameController.laneToWorldY(lane);
                    float dx = Math.abs(zx - tileX);
                    float dy = Math.abs(zy - tileY);
                    if (dx < range && dy < range) {
                        targets.add(z);
                    }
                }
            }
        }

        Tile frontTile = ctx.getMap().getTileAt(plantCol + 1, plantLane);
        boolean frontTileHasDestructible = false;
        if (frontTile != null && (frontTile.getTags().contains(TileTags.GRAVE)
                || frontTile.getTags().contains(TileTags.ICE_BLOCK))) {
            frontTileHasDestructible = true;
        }

        return !targets.isEmpty() || frontTileHasDestructible;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        attacked=false;
        stateTime=0;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (!attacked && stateTime>bonkChoyConfig.intervalSeconds/2){
            if (targets != null) {
                for (Zombie z : targets) {
                    z.takeDamage(bonkChoyConfig.baseDamage);
                }
            }
            int plantCol = plant.getCol();
            int plantLane = plant.getLane();
            for (int lane = plantLane - 1; lane <= plantLane + 1; lane++) {
                for (int col = plantCol - 1; col <= plantCol + 1; col++) {
                    Tile tile = ctx.getMap().getTileAt(col, lane);
                    if (tile != null) tile.processHit(bonkChoyConfig.baseDamage);
                }
            }
            attacked=true;
        }
        if (stateTime>=bonkChoyConfig.intervalSeconds) plant.changeState(new PlantIdleState());
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        targets=null;
        attacked=false;
        stateTime=0;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath,bonkChoyConfig.label,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
