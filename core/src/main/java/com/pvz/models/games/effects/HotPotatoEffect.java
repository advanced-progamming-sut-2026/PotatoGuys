package com.pvz.models.games.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.Effect;
import com.pvz.models.games.GameContext;

public class HotPotatoEffect extends Effect {
    private static final String STEAM_PAM = "768/FULL/EFFECTS/HOTPOTATO_ICEBLOCK_STEAMFX/HOTPOTATO_ICEBLOCK_STEAMFX.PAM";
    private static final String STEAM_CLIP = "animation";
    private static final float STEAM_DURATION = 3.77f;

    private static final String PUDDLE_PAM = "768/FULL/EFFECTS/HOTPOTATO_ICEBLOCK_PUDDLE/HOTPOTATO_ICEBLOCK_PUDDLE.PAM";
    private static final String PUDDLE_CLIP = "animation";
    private static final float PUDDLE_DURATION = 1.66f;

    private float stateTime2;

    public HotPotatoEffect(GameContext ctx, Vector2 pos) {
        super(ctx, pos);
        stateTime2 = 0;
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime > 1f) {
            stateTime2 += dt;
            if (stateTime > STEAM_DURATION) {
                dispose();
            }
        }
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();

        if (stateTime > 1f && stateTime2 <= PUDDLE_DURATION) {
            FrameConfig puddle = new FrameConfig(PUDDLE_PAM, PUDDLE_CLIP, stateTime2, pos, new Vector2(0.7f, 0.7f),
                    null,
                    false);
            frameConfigs.add(puddle);
        }

        FrameConfig steam = new FrameConfig(STEAM_PAM, STEAM_CLIP, stateTime, pos, new Vector2(0.7f, 0.7f), null,
                false);
        frameConfigs.add(steam);

        return frameConfigs;
    }

}
