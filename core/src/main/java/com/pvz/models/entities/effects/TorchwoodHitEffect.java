package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.games.GameContext;

/**
 * Plays a TORCHWOOD_HIT_EFFECTS clip on a zombie (per-bite retaliation and
 * plant-food hits). One-shot effect anchored at the zombie's position.
 */
public class TorchwoodHitEffect extends Effect {

    public static final String PAM_PATH = "768/INITIAL/EFFECTS/TORCHWOOD_HIT_EFFECTS/TORCHWOOD_HIT_EFFECTS.PAM";
    private static final float DEFAULT_DURATION = 0.5f;
    private static final float SCALE = 0.65f;

    private final String clip;
    private final float clipDuration;

    public TorchwoodHitEffect(GameContext ctx, Vector2 pos, String clip) {
        super(ctx, pos);
        this.clip = clip;
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        float d = catalog != null ? catalog.getClipDuration(PAM_PATH, clip) : -1f;
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
        frameConfigs.add(new FrameConfig(PAM_PATH, clip, stateTime, pos,
                new Vector2(SCALE, SCALE), null, false));
        return frameConfigs;
    }
}