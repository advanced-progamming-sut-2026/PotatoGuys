package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.games.GameContext;

public class ChillWind extends Effect {
    private static final String PAM_PATH = "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";
    private static final String CLIP = "animation";

    private float duration;

    public ChillWind(GameContext ctx, Vector2 pos) {
        super(ctx, pos);
        duration = AnimationCatalog.instance.getClipDuration(PAM_PATH, CLIP);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime >= duration) {
            dispose();
        }
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        Vector2 scale = new Vector2(0.65f, 0.65f);
        frameConfigs.add(new FrameConfig(PAM_PATH, CLIP, stateTime, pos, scale, null, false));
        return frameConfigs;
    }
}
