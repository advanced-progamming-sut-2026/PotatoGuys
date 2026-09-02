package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.games.GameContext;

/**
 * Plays Torchwood's explosion clip from its own PAM at a position, used when
 * the plant is destroyed. One-shot effect.
 */
public class TorchwoodExplosionEffect extends Effect {

    public static final String PAM_PATH = "768/INITIAL/PLANT/TORCHWOOD/TORCHWOOD.PAM";
    private static final String CLIP = "explosion";
    private static final float DEFAULT_DURATION = 1.7f;
    private static final float SCALE = 0.65f;

    private final float clipDuration;

    public TorchwoodExplosionEffect(GameContext ctx, Vector2 pos) {
        super(ctx, pos);
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        float d = catalog != null ? catalog.getClipDuration(PAM_PATH, CLIP) : -1f;
        this.clipDuration = d > 0f ? d : DEFAULT_DURATION;
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
        frameConfigs.add(new FrameConfig(PAM_PATH, CLIP, stateTime, pos,
                new Vector2(SCALE, SCALE), null, false));
        return frameConfigs;
    }
}