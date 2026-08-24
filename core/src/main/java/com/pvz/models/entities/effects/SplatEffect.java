package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.games.GameContext;

public class SplatEffect extends Effect {
    private final String pamPath;
    private final String clip;
    private final float scale;
    private final float clipDuration;

    public SplatEffect(GameContext ctx, Vector2 pos, String pamPath, String clip, float scale) {
        super(ctx, pos);
        this.pamPath = pamPath;
        this.clip = clip;
        this.scale = scale;
        this.clipDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, clip);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime >= clipDuration) {
            dispose();
        }
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        frameConfigs.add(new FrameConfig(pamPath, clip, stateTime, pos, new Vector2(scale, scale), null, false));
        return frameConfigs;
    }
}
