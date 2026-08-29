package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.behaviors.WaterBehavior;

public class Water extends Effect {
    private static final String WATER_UNDERLAYER_PAM = "768/FULL/BACKGROUNDS/WATER_UNDERLAYER/WATER_UNDERLAYER.PAM";
    private static final String WATER_UNDERLAYER_CLIP = "Water";

    private static final String WATER_SQUARE_PAM = "768/FULL/BACKGROUNDS/WATER_SQUARE/WATER_SQUARE.PAM";
    private static final String WATER_SQUARE_CLIP = "Water";

    private static final String WATER_UPPERLAYER_PAM = "768/FULL/BACKGROUNDS/WAVE_UPPERLAYER/WAVE_UPPERLAYER.PAM";
    private static final String WATER_UPPERLAYER_CLIP = "water";

    public Water(GameContext ctx, Vector2 pos) {
        super(ctx, pos);
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();

        FrameConfig waterUnderlayer = new FrameConfig(WATER_UNDERLAYER_PAM, WATER_UNDERLAYER_CLIP, stateTime,
                new Vector2(pos.x - GameMap.TILE_WIDTH * 2, 324),
                new Vector2(0.6f, 0.6f), null, true);
        waterUnderlayer.setColor(1f, 1f, 1f, 1f);
        frameConfigs.add(waterUnderlayer);
        /*
         * // Water UnderLayer
         * for (int lane = 0; lane < ctx.getMap().getLanes(); lane++) {
         * Vector2 layerPos = new Vector2(pos.x, pos.y + (lane - 2) *
         * GameMap.TILE_HEIGHT);
         * FrameConfig waterUnderlayer = new FrameConfig(WATER_UNDERLAYER_PAM,
         * WATER_UNDERLAYER_CLIP, stateTime,
         * layerPos,
         * new Vector2(0.4f, 0.08f), null, true);
         * waterUnderlayer.setColor(1f, 1f, 1f, 1f);
         * frameConfigs.add(waterUnderlayer);
         * }
         */

        for (int lane = 0; lane < ctx.getMap().getLanes(); lane++) {
            for (int col = 0; col < ctx.getMap().getColumns(); col++) {
                for (TileBehavior tb : ctx.getTileAt(col, lane).getBehaviors()) {
                    if (tb instanceof WaterBehavior) {
                        if (lane % 2 == 0 && col % 2 == 0 || lane % 2 == 1 && col % 2 == 1) {
                            frameConfigs.add(new FrameConfig(WATER_SQUARE_PAM, WATER_SQUARE_CLIP, stateTime,
                                    ctx.getTileAt(col, lane).getPosition(), new Vector2(0.5f, 0.5f), null, true));
                        }
                    }
                }
            }
        }

        FrameConfig waterUpperLayer = new FrameConfig(WATER_UPPERLAYER_PAM, WATER_UPPERLAYER_CLIP, stateTime,
                new Vector2(pos.x, 324),
                new Vector2(0.6f, 0.6f), null, true);
        frameConfigs.add(waterUpperLayer);
        return frameConfigs;
    }

    public void setPos(Vector2 pos) {
        this.pos.set(pos);
    }

}
