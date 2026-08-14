package com.pvz.models.entities.effects;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.actions.explosive.potato_mine.ExplosionIntensity;
import com.pvz.models.entities.plants.actions.explosive.potato_mine.ExplosionType;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.games.GameContext;

public class Explosion extends Effect{
    private static final String POTATO_MINE_EXPLOSION_PAM_PATH="768/INITIAL/EFFECTS/POTATOMINE_EXPLOSION/POTATOMINE_EXPLOSION.PAM";
    private static final String PRIMAL_POTATO_MINE_EXPLOSION_PAM_PATH="768/INITIAL/EFFECTS/ESCAPEROOT_EXPLOSION_PRIMALPOTATOMINE/ESCAPEROOT_EXPLOSION_PRIMALPOTATOMINE.PAM";
    private static final String LOW_INTENSITY_CLIP="animation";
    private static final String HIGH_INTENSITY_CLIP="animation2";
    private static final String INSANE_INTENSITY_CLIP="animation3";

    ExplosionType type;
    ExplosionIntensity intensity;
    String pamPath;
    String clip;
    float clipDuration;
    public Explosion(GameContext ctx,Vector2 pos, ExplosionType type, ExplosionIntensity intensity){
        super(ctx,pos);
        this.type=type;
        this.intensity=intensity;
        switch (type){
            case POTATO_MINE -> pamPath=POTATO_MINE_EXPLOSION_PAM_PATH;
            case PRIMAL_POTATO_MINE -> pamPath=PRIMAL_POTATO_MINE_EXPLOSION_PAM_PATH;
        }
        switch (intensity){
            case LOW -> clip=LOW_INTENSITY_CLIP;
            case HIGH -> clip=HIGH_INTENSITY_CLIP;
            case INSANE -> clip=INSANE_INTENSITY_CLIP;
        }
        clipDuration=AnimationCatalog.getInstance().getClipDuration(pamPath,clip);
    }

    @Override
    public void enter() {
        super.enter();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime>=clipDuration){
            dispose();
        }
    }

    @Override
    public FrameConfig draw() {
        return new FrameConfig(pamPath,clip,stateTime,pos,new Vector2(0.65f,0.65f),null,false);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
