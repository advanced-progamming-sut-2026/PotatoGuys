package com.pvz.models.entities.zombies.skills;

import java.util.Map;

import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.config.GargantuarSkillConfig;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.fsm.ImpFlyState;
import com.pvz.models.entities.zombies.fsm.WalkState;
import com.pvz.models.entities.zombies.fsm.ZombieState;
import com.pvz.models.games.GameContext;

public class GargantuarSkill extends ZombieState {

    private static final int IMP_TARGET_COL = 2;
    private static final float IMP_OFFSET_X = 20f;
    private static final float IMP_OFFSET_Y = 80f;

    private final String impAlias;
    private final float throwHpFraction;
    private boolean secondPhase;

    public GargantuarSkill(GargantuarSkillConfig config) {
        super(config);
        this.impAlias = config.impType;
        this.throwHpFraction = config.throwHpFraction;
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        if (zombie.isImpAlreadyThrown()) return false;
        if (zombie.getMaxHp() <= 0f) return false;
        float hpFraction = zombie.getHp() / zombie.getMaxHp();
        return hpFraction <= throwHpFraction;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        if (zombie.isThrowInProgress()) {
            stateTime = zombie.getThrowProgress();
            secondPhase = zombie.isThrowImpSpawned();
        } else {
            stateTime = 0f;
            secondPhase = false;
            zombie.setThrowInProgress(true);
            zombie.setThrowImpSpawned(false);
        }
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        float fireDur = getClipDuration(zombie, "fire");
        float cannonDur = getClipDuration(zombie, "cannon_fire");

        if (!secondPhase) {
            stateTime += dt;
            zombie.setThrowProgress(stateTime);
            if (stateTime >= fireDur) {
                secondPhase = true;
                stateTime = 0f;
                spawnImp(zombie, ctx);
            }
        } else {
            stateTime += dt;
            zombie.setThrowProgress(stateTime);
            if (stateTime >= cannonDur) {
                zombie.setThrowInProgress(false);
                return new WalkState();
            }
        }
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        zombie.setThrowInProgress(false);
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        String clip = secondPhase ? "cannon_fire" : "fire";
        return zombie.drawClip(clip, stateTime, false);
    }

    @Override
    public String getName() {
        return "ThrowImp[" + impAlias + "]";
    }

    private float getClipDuration(Zombie zombie, String clipName) {
        ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
        if (anim != null && anim.pamFilePath != null) {
            AnimationCatalog catalog = AnimationCatalog.getInstance();
            if (catalog != null) {
                float dur = catalog.getClipDuration(anim.pamFilePath, clipName);
                if (dur > 0f) return dur;
            }
        }
        return config != null && config.durationSeconds > 0f ? config.durationSeconds : 1f;
    }

    private void spawnImp(Zombie zombie, GameContext ctx) {
        zombie.setThrowImpSpawned(true);
        zombie.markImpThrown();
        int lane = GameController.worldYtoLane(zombie.getY());
        float launchX = zombie.getX() + IMP_OFFSET_X;
        float launchY = zombie.getY() + IMP_OFFSET_Y;
        Zombie imp = new ZombieFactory().create(impAlias, zombie.getX(), lane, ctx, 1, 2);
        int gargCol = GameController.worldXtoCol(zombie.getX());
        int targetCol = Math.max(1, gargCol - IMP_TARGET_COL);
        imp.setPendingInitialState(new ImpFlyState(launchX, launchY, zombie.getX(), zombie.getY(), lane, ctx));
        ctx.spawnZombie(imp);
        ctx.log("Gargantuar threw Imp [" + impAlias + "] to column "
                + targetCol + " in lane " + lane + " (parabolic flight)!");
    }

}
