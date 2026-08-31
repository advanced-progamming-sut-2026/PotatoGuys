package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

public class DeadLine extends Effect {
    private static final String FLOWER_PAM = "768/INITIAL/EFFECTS/STAR_OBJECTIVE_FLOWER/STAR_OBJECTIVE_FLOWER.PAM";

    int col;

    public DeadLine(GameContext ctx, int column) {
        super(ctx, new Vector2(0, GameController.laneToWorldY(-1)));
        this.col = column;
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();

        float x = GameController.colToWorldX(col) + GameMap.TILE_WIDTH / 2f;
        for (int lane = 0; lane < ctx.getMap().getLanes(); lane++) {
            float y = GameController.laneToWorldY(lane);
            frameConfigs.add(new FrameConfig(FLOWER_PAM, "idle", stateTime, new Vector2(x, y),
                    new Vector2(0.65f, 0.65f), null, true));
        }

        return frameConfigs;
    }

}
