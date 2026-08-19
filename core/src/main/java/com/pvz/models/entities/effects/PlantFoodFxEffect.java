package com.pvz.models.entities.effects;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.GameContext;

public class PlantFoodFxEffect extends Effect {

    private static final String PAM_PATH =
            "768/INITIAL/EFFECTS/PLANTFOOD_FX/PLANTFOOD_FX.PAM";
    private static final String CLIP = "plantfood";
    private static final float SCALE = 1.35f;
    private static final float OFFSET_X = 20f;
    private static final float OFFSET_Y = 120f;
    private static final float DURATION = 2.5f;

    public PlantFoodFxEffect(GameContext ctx, Vector2 plantPos) {
        super(ctx, new Vector2(
                plantPos.x + OFFSET_X,
                plantPos.y + OFFSET_Y));
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime >= DURATION) {
            dispose();
        }
    }

    @Override
    public FrameConfig draw() {
        return new FrameConfig(PAM_PATH, CLIP, stateTime, pos,
                new Vector2(SCALE, SCALE), null, false);
    }
}
