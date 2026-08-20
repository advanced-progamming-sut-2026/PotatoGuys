package com.pvz.models.entities.zombies.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

import java.util.Map;

/**
 * Permanent frozen state for zombies encased in ice blocks.
 * Unlike {@link FrozenState} which expires after a duration, this state
 * persists until the ice HP is depleted by fire projectiles.
 */
public class IceBlockFrozenState extends ZombieState {
    private static final float MAX_ICE_HP = 600f;
    private static final float FLASH_DURATION = 0.28f;

    private float iceHp;
    private float flashTimer;
    private String pamPath;
    private String clip;
    private float clipTime;
    private Map<String, Boolean> partVisibility;

    public IceBlockFrozenState(String pamPath, String clip, float clipTime,
                               Map<String, Boolean> partVisibility) {
        super(null);
        this.iceHp = MAX_ICE_HP;
        this.pamPath = pamPath;
        this.clip = clip;
        this.clipTime = clipTime;
        this.partVisibility = partVisibility;
    }

    /**
     * Creates an IceBlockFrozenState using the zombie's default walk frame info.
     * Use this when setting the state before the zombie has entered the game.
     */
    public static IceBlockFrozenState fromZombie(Zombie zombie) {
        FrameConfig walkFrame = zombie.drawClip("walk", 0f, true);
        return new IceBlockFrozenState(
                walkFrame.pamPath, walkFrame.label, 0f, walkFrame.partsVisibility);
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        super.onEnter(zombie, ctx);
        zombie.setFrozenInIceBlock(true);
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        if (flashTimer > 0f) {
            flashTimer -= dt;
        }
        return this;
    }

    /**
     * Apply damage to the ice block. Fire damage instant-melts.
     * @return true if the ice block was destroyed
     */
    public boolean takeIceDamage(float amount, boolean isFire) {
        if (isFire) {
            iceHp = 0f;
        } else {
            iceHp = Math.max(0f, iceHp - amount);
        }
        flashTimer = FLASH_DURATION;
        return iceHp <= 0f;
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        if (pamPath == null) {
            FrameConfig walkFrame = zombie.drawClip("walk", 0f, true);
            pamPath = walkFrame.pamPath;
            clip = walkFrame.label;
            partVisibility = walkFrame.partsVisibility;
        }
        FrameConfig fc = new FrameConfig(pamPath, clip, clipTime, zombie.getPosition(),
                new Vector2(0.65f, 0.65f), partVisibility, false);
        if (flashTimer > 0f) {
            fc.setColor(5f, 5f, 5f, 0.6f);
        } else {
            fc.setColor(0.4f, 0.6f, 1f, 1f);
        }
        return fc;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        super.onExit(zombie, ctx);
        zombie.setFrozenInIceBlock(false);
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        return false;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
    }

    @Override
    public String getName() {
        return "IceBlockFrozen";
    }

    public float getIceHp() {
        return iceHp;
    }
}
