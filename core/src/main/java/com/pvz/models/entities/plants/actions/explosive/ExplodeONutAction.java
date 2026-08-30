package com.pvz.models.entities.plants.actions.explosive;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.Explosion;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.NutConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

import java.util.ArrayList;
import java.util.List;

public class ExplodeONutAction extends PlantAction {
    NutConfig config;
    String currentClip;

    public ExplodeONutAction(NutConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return plant.getHp() <= plant.getSheet().getBaseHp() * 0.75f;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        currentClip = config.damage1Clip;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);

        float hp = plant.getHp();
        float baseHp = plant.getSheet().getBaseHp();

        if (hp <= baseHp * 0.5f && hp > baseHp * 0.25f) {
            currentClip = config.damage2Clip;
        } else if (hp <= baseHp * 0.25f && hp > 0) {
            currentClip = config.damage3Clip;
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        ctx.addEffect(new Explosion(ctx, plant.getPosition(),
                ExplosionType.PRIMAL_POTATO_MINE, ExplosionIntensity.LOW));
        List<Zombie> targets = new ArrayList<>();
        for (int col = plant.getCol() - 1; col <= plant.getCol() + 1; col++) {
            for (int lane = plant.getLane() - 1; lane <= plant.getLane() + 1; lane++) {
                targets.addAll(ctx.getZombiesAt(col, lane));
            }
        }
        for (Zombie z : targets) {
            z.takeDamage(effectiveDamage(plant), false, true);
        }
    }

    /** Base explosion damage + the "Explode Dmg +200" level flag. */
    private float effectiveDamage(Plant plant) {
        float damage = config.baseDamage;
        if (plant.getUnlockedFlags().contains("Explode Dmg +200")) {
            damage += 200f;
        }
        return damage;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 scale = new Vector2(0.65f, 0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath, currentClip, stateTime, plant.getPosition(), scale, null,
                false);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
