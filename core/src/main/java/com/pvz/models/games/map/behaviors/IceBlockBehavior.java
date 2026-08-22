package com.pvz.models.games.map.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.models.games.map.tile.TileTags;

public class IceBlockBehavior implements TileBehavior {
    private static final String PAM_PATH = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    private static final String IDLE_CLIP = "freeze_idle";
    private static final float FLASH_DURATION = 0.28f;
    private float iceHp = 600f;
    private Object entityInside; // Can be Plant or Zombie
    float stateTime;
    private float flashTimer;

    public IceBlockBehavior(Object entity) {
        this.entityInside = entity;
        if (entity instanceof Plant p) {
            p.incrementFreezeLevel(); // Set it to frozen state
        }
        // Zombies are already frozen via StatusEffect or this behavior

        this.stateTime = 0;
    }

    @Override
    public void onProjectileHit(Projectile p, Tile tile) {
        boolean isFire = p.getType() == ProjectileType.FIRE_PEA;
        float damage = p.getDamage();
        flashTimer = FLASH_DURATION;

        if (isFire) {
            iceHp = 0f;
        } else {
            iceHp = Math.max(0f, iceHp - damage);
        }

        if (iceHp <= 0f) {
            tile.removeBehavior(this);
            tile.getTags().removeAll(tile.getTags().stream().filter(t -> t.equals(TileTags.ICE_BLOCK)).toList());
            // Release entity
            if (entityInside instanceof Plant plant) {
                plant.takeIceDamage(0f, true); // Melts it
            }
            // For Zombie, the paralysis effect will expire naturally
        }
    }

    @Override
    public void update(GameContext ctx, Tile tile, float dt) {
        TileBehavior.super.update(ctx, tile, dt);
        stateTime += dt;
        if (flashTimer > 0f)
            flashTimer = Math.max(0f, flashTimer - dt);
    }

    public void takeDamage(float amount, boolean isFire) {
        flashTimer = FLASH_DURATION;
        if (isFire) {
            iceHp = 0f;
        } else {
            iceHp = Math.max(0f, iceHp - amount);
        }
    }

    public float getIceHp() {
        return iceHp;
    }

    public Object getEntityInside() {
        return entityInside;
    }

    @Override
    public String getName() {
        return "IceBlock";
    }

    @Override
    public FrameConfig draw(Tile tile) {
        TileBehavior.super.draw(tile);
        Vector2 scale = new Vector2(0.65f, 0.65f);
        Vector2 pos = new Vector2(GameController.colToWorldX(tile.getCol()),
                GameController.laneToWorldY(tile.getLane()));
        FrameConfig frameConfig = new FrameConfig(PAM_PATH, IDLE_CLIP, stateTime, pos, scale, null, false);
        if (flashTimer > 0f) {
            frameConfig.setColor(5f, 5f, 5f, 0.4f);
        } else {
            frameConfig.setColor(1, 1, 1, 0.55f);
        }
        return frameConfig;
    }
}
