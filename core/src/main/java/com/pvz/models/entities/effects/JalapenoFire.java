package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.games.GameContext;

public class JalapenoFire extends Effect {
    private static final String PAM_PATH = "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM";

    private static final String CLIP_0 = "idle";
    private final float clip0Duration;
    boolean clip0Played;

    private static final String CLIP_1 = "idle2";
    private final float clip1Duration;
    boolean clip1Played;

    private static final String CLIP_2 = "idle3";
    private final float clip2Duration;

    private String currentClip;

    public JalapenoFire(GameContext ctx, Vector2 pos) {
        super(ctx, pos);
        clip0Duration = AnimationCatalog.getInstance().getClipDuration(PAM_PATH, CLIP_0);
        clip1Duration = AnimationCatalog.getInstance().getClipDuration(PAM_PATH, CLIP_1);
        clip2Duration = AnimationCatalog.getInstance().getClipDuration(PAM_PATH, CLIP_2);

        clip0Played = false;
        clip1Played = false;

        currentClip = CLIP_0;
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (!clip0Played && stateTime >= clip0Duration) {
            currentClip = CLIP_1;
            clip0Played = true;
            stateTime = 0;
        } else if (!clip1Played && stateTime >= clip1Duration) {
            currentClip = CLIP_2;
            clip1Played = true;
            stateTime = 0;
        } else if (currentClip.equals(CLIP_2) && stateTime >= clip2Duration) {
            dispose();
        }
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        frameConfigs.add(
                new FrameConfig(PAM_PATH, currentClip, stateTime, pos, new Vector2(0.65f, 0.65f), null, false));
        return frameConfigs;
    }
}
