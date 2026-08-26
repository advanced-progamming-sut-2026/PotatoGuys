package com.pvz.models.entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

public class LawnMower extends Entity {
    private static final String PAM_PATH = "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
    private static final String PAM_LABEL_IDLE = "idle";
    private static final String PAM_LABEL_ATTACK = "attack";
    private static final float VELOCITY = 300f;

    GameContext ctx;
    boolean triggered;
    float stateTime;

    public LawnMower(GameContext ctx, int lane) {
        position.set(GameController.colToWorldX(-1), GameController.laneToWorldY(lane));
        velocity.set(VELOCITY, 0f);
        setHitbox(new Hitbox(this, position.x, position.y, 60f, 80f) {
            @Override
            public void onCollision(Hitbox onHit) {
                if (onHit.getOwner() instanceof Zombie zombie && !zombie.isDead()) {
                    if (!triggered) {
                        triggered = true;
                        ctx.log("[LawnMower] activated in lane " + GameController.worldYtoLane(position.y) + "!");
                    }
                    zombie.takeDamage(100000);
                    if (zombie.isDead()) {
                        ctx.getGameStats().onLawnmowerKill(1);
                    }
                }
            }
        });
        this.ctx = ctx;
        triggered = false;
        stateTime = 0;
    }

    @Override
    public void enter() {

    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (triggered) {
            position.x += VELOCITY * dt;
            syncHitbox();
        }
        if (position.x > GameController.colToWorldX(ctx.getMap().getColumns() + 1)) {
            dispose();
        }
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        if (!triggered)
            frameConfigs.add(new FrameConfig(PAM_PATH, PAM_LABEL_IDLE, stateTime, position,
                    new Vector2(0.75f, 0.75f), null, true));
        else
            frameConfigs.add(new FrameConfig(PAM_PATH, PAM_LABEL_ATTACK, stateTime, position,
                    new Vector2(0.75f, 0.75f), null, true));
        return frameConfigs;
    }

    @Override
    public void dispose() {
        GameEngine.getInstance().getToRemove().add(this);
        ctx.removeHitbox(getHitbox());
        ctx.getLawnMowers()[GameController.worldYtoLane(position.y)] = null;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public int getLane() {
        return GameController.worldYtoLane(position.y);
    }
}
