package com.pvz.models.entities.effects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.GameContext;

public class DetachedArmEffect extends Effect {

    private static final float DURATION = 2f;
    private static final float DROP_SPEED = 70f;
    private static final float FADE_START = 0.5f;
    private static final float MAX_DROP = 48f;

    private final String pamPath;
    private final float scale;
    private final Map<String, Boolean> partsVisibility;
    private float dropOffset;

    public DetachedArmEffect(GameContext ctx, Vector2 pos, String pamPath,
                             float scale, List<String> armPartNames,
                             List<String> hidePartNames) {
        super(ctx, pos);
        this.pamPath = pamPath;
        this.scale = scale;
        this.partsVisibility = new HashMap<>();

        if (hidePartNames != null) {
            for (String part : hidePartNames) {
                partsVisibility.put(part, false);
            }
        }
        for (String part : armPartNames) {
            partsVisibility.put(part, true);
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime >= DURATION) {
            dispose();
            return;
        }
        dropOffset = Math.min(dropOffset + DROP_SPEED * dt, MAX_DROP);
    }

    @Override
    public FrameConfig draw() {
        float alpha = 1f;
        if (stateTime > FADE_START) {
            float fadeProgress = (stateTime - FADE_START) / (DURATION - FADE_START);
            alpha = Math.max(0f, 1f - fadeProgress);
        }
        Vector2 drawPos = new Vector2(pos.x, pos.y - dropOffset);
        FrameConfig fc = new FrameConfig(pamPath, "idle", stateTime,
                drawPos, new Vector2(scale, scale), partsVisibility, true);
        fc.setColor(1f, 1f, 1f, alpha);
        return fc;
    }
}
