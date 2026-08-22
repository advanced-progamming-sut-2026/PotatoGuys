package com.pvz.models.entities.effects;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

public class HeadDeathEffect extends Effect {

    private static final float TOTAL_TIME = 1.5f;
    private static final float GRAVITY = 500f;
    private static final float MAX_COLUMNS = 1.5f;

    private final String pamPath;
    private final float scale;
    private final Map<String, Boolean> partsVisibility;
    private final float vx;
    private final float vy;
    private final float startX;

    public HeadDeathEffect(GameContext ctx, Vector2 pos, String pamPath, float scale) {
        super(ctx, pos);
        this.pamPath = pamPath;
        this.scale = scale;
        this.startX = pos.x;

        float distance = MathUtils.random(-MAX_COLUMNS, MAX_COLUMNS) * GameMap.TILE_WIDTH;
        float totalTime = TOTAL_TIME;
        vx = distance / totalTime;
        vy = 0.5f * GRAVITY * totalTime;

        partsVisibility = new HashMap<>();
        partsVisibility.put("particle_head", true);
        partsVisibility.put("particle_arm", false);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime >= TOTAL_TIME) {
            dispose();
            return;
        }
        pos.x = startX + vx * stateTime;
    }

    @Override
    public FrameConfig draw() {
        float height = vy * stateTime - 0.5f * GRAVITY * stateTime * stateTime;
        Vector2 drawPos = new Vector2(pos.x, pos.y + height);
        float alpha = 1f;
        if (stateTime > TOTAL_TIME * 0.6f) {
            alpha = 1f - (stateTime - TOTAL_TIME * 0.6f) / (TOTAL_TIME * 0.4f);
            alpha = Math.max(0f, alpha);
        }
        FrameConfig fc = new FrameConfig(pamPath, "particles", stateTime,
                drawPos, new Vector2(scale, scale), partsVisibility, true);
        fc.setColor(1f, 1f, 1f, alpha);
        return fc;
    }
}
