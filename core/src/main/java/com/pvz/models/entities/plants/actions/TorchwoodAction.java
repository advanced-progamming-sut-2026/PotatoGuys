package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.JalapenoFire;
import com.pvz.models.entities.effects.TorchwoodExplosionEffect;
import com.pvz.models.entities.effects.TorchwoodHitEffect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.TorchwoodConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.fsm.EatState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

/**
 * Torchwood's passive action. Takes over the FSM (like a wall-nut) so it can
 * retaliate each time a zombie bites it, and exposes the whole-lane burn
 * triggered from {@link Plant#kill()}.
 */
public class TorchwoodAction extends PlantAction {

    private final TorchwoodConfig config;
    private float biteTimer = 0f;

    public TorchwoodAction(TorchwoodConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return true;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        biteTimer = 0f;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        biteTimer += dt;
        if (biteTimer < config.biteIntervalSeconds) {
            return;
        }
        biteTimer = 0f;
        for (Zombie eater : eaters(plant, ctx)) {
            if (eater.isDead()) {
                continue;
            }
            eater.takeDamage(config.biteDamage);
            ctx.addEffect(new TorchwoodHitEffect(ctx, eater.getPosition(), config.biteHitClip));
        }
    }

    /**
     * Finds every zombie currently eating this plant. Scans the whole board (not
     * just the plant's column) because an eater's center sits one tile to the
     * left of the plant, so grid-column lookups miss it.
     */
    private List<Zombie> eaters(Plant plant, GameContext ctx) {
        List<Zombie> eaters = new ArrayList<>();
        for (Zombie zombie : ctx.getZombies()) {
            if (!zombie.isDead()
                    && zombie.getCurrentState() instanceof EatState eatState && eatState.getTarget() == plant) {
                eaters.add(zombie);
            }
        }
        return eaters;
    }

    /**
     * Called from {@link Plant#kill()} when Torchwood dies: burns the whole lane
     * like Jalapeno and plays the Torchwood explosion clip at its position.
     */
    public void onDeath(Plant plant, GameContext ctx) {
        for (int i = 0; i < ctx.getMap().getColumns(); i++) {
            Vector2 pos = new Vector2(GameController.colToWorldX(i),
                    GameController.laneToWorldY(plant.getLane()));
            ctx.addEffect(new JalapenoFire(ctx, pos));
        }
        ctx.addEffect(new TorchwoodExplosionEffect(ctx, plant.getPosition()));

        for (Zombie z : new ArrayList<>(ctx.getZombiesInLane(plant.getLane()))) {
            z.takeDamage(config.deathDamage, false, true);
        }
        for (int i = 0; i < ctx.getMap().getColumns(); i++) {
            Tile tile = ctx.getMap().getTileAt(i, plant.getLane());
            if (tile != null) {
                tile.processHit(config.deathDamage);
            }
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        Vector2 pos = new Vector2(GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.7f, 0.7f);
        return new FrameConfig(pam.pamFilePath, pam.idleLabel, stateTime, pos, scale, null, true);
    }

    @Override
    public String getLabel() {
        return "Torchwood";
    }
}