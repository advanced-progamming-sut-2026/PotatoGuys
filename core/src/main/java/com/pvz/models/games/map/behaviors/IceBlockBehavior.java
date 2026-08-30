package com.pvz.models.games.map.behaviors;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.models.games.map.tile.TileTags;

public class IceBlockBehavior implements TileBehavior {
    private static final float FLASH_DURATION = 0.28f;
    private float iceHp = 600f;
    private Object entityInside; // Can be Plant or Zombie
    float stateTime;
    private float flashTimer;
    private Tile tile;

    public IceBlockBehavior(Tile tile, Object entity) {
        this.entityInside = entity;
        this.tile = tile;

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

        p.destroy();
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
        return null;
    }

    @Override
    public void processHit(float damage) {
        iceHp = Math.max(0f, iceHp - damage);
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
    public boolean canPlant(PlantCard p, Tile tile) {
        return p.getPlant().getType() == PlantType.HotPotato;
    }
}
