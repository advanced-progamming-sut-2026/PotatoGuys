package com.pvz.models.entities.plants.actions;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.GrowthMeleeConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

/**
 * AoE melee action for plants that grow through multiple stages over time,
 * dealing increasing damage at each stage (e.g. Kiwibeast).
 *
 * <p>Behaviour:
 * <ul>
 *   <li>Accumulates time while idle; when {@code stateTime >= intervalSeconds} and a zombie
 *       is within range, the plant attacks all zombies in the lane up to
 *       {@code attackRangeFactor * TILE_WIDTH}.</li>
 *   <li>After each attack, checks whether enough time has elapsed to advance to the next
 *       growth stage. If so, plays the growth clip and increments the stage.</li>
 *   <li>During a growth clip transition the attack does not trigger.</li>
 * </ul>
 */
public class GrowthMeleeAction extends PlantAction {

    private final GrowthMeleeConfig config;
    private int currentStage = 0;
    private float growthAccumulator = 0f;
    private boolean playingGrowth = false;
    private float growthClipDuration = 0f;
    private String currentGrowthClip = "";

    public GrowthMeleeAction(GrowthMeleeConfig config) {
        this.config = config;
    }

    // ── Trigger logic ────────────────────────────────────────────────────────

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;

        if (playingGrowth) {
            return false;
        }

        growthAccumulator += dt;

        if (stateTime < config.intervalSeconds) {
            return false;
        }

        float attackRange = config.attackRangeFactor * GameMap.TILE_WIDTH;
        float plantX = GameController.colToWorldX(plant.getCol());
        for (Zombie z : ctx.getZombiesInLane(plant.getLane())) {
            float distance = z.getX() - plantX;
            if (distance > 0 && distance < attackRange) {
                return true;
            }
        }
        return false;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;

        if (playingGrowth) {
            PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
            growthClipDuration = AnimationCatalog.getInstance()
                .getClipDuration(pam.pamFilePath, currentGrowthClip);
            return;
        }

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        String attackClip = resolveAttackLabel(currentStage);
        growthClipDuration = AnimationCatalog.getInstance()
            .getClipDuration(pam.pamFilePath, attackClip);
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (plant.isDead()) return;

        if (playingGrowth) {
            if (stateTime >= growthClipDuration) {
                playingGrowth = false;
                plant.changeState(new PlantIdleState());
            }
            return;
        }

        if (stateTime >= config.intervalSeconds) {
            float attackRange = config.attackRangeFactor * GameMap.TILE_WIDTH;
            float plantX = GameController.colToWorldX(plant.getCol());
            float damage = config.baseDamage * resolveDamageMultiplier(currentStage);

            for (Zombie z : ctx.getZombiesInLane(plant.getLane())) {
                float distance = z.getX() - plantX;
                if (distance > 0 && distance < attackRange) {
                    z.takeDamage(damage);
                }
            }
            ctx.log("[Action] Kiwibeast dealt " + (int) damage + " AoE damage (stage " + currentStage + ").");

            checkAdvanceGrowth(plant, ctx);
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        stateTime = 0f;
    }

    @Override
    public String getLabel() { return "GrowthMelee"; }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.65f, 0.65f);

        String clip;
        if (playingGrowth) {
            clip = currentGrowthClip;
        } else {
            clip = resolveAttackLabel(currentStage);
        }
        return new FrameConfig(pam.pamFilePath, clip, stateTime, pos, scale, null, false);
    }

    // ── Helpers used by PlantIdleState ────────────────────────────────────────

    public String getCurrentIdleLabel() {
        return resolveIdleLabel(currentStage);
    }

    public float getBaseDamage() {
        return config.baseDamage * resolveDamageMultiplier(currentStage);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void checkAdvanceGrowth(Plant plant, GameContext ctx) {
        if (currentStage < config.stageIntervals.length
            && growthAccumulator >= config.stageIntervals[currentStage]) {
            int fromStage = currentStage;
            currentStage++;
            growthAccumulator = 0f;
            stateTime = 0f;

            String growthClip = resolveGrowthLabel(fromStage);
            if (growthClip != null) {
                PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
                growthClipDuration = AnimationCatalog.getInstance()
                    .getClipDuration(pam.pamFilePath, growthClip);
                currentGrowthClip = growthClip;
                playingGrowth = true;
                ctx.log("[Action] Kiwibeast advanced to stage " + currentStage + ".");
            }
        } else {
            plant.changeState(new PlantIdleState());
        }
    }

    private float resolveDamageMultiplier(int stage) {
        if (config.damageMultipliers != null && stage < config.damageMultipliers.length) {
            return config.damageMultipliers[stage];
        }
        return 1f;
    }

    private String resolveIdleLabel(int stage) {
        if (config.idleLabels != null && stage < config.idleLabels.length) {
            return config.idleLabels[stage];
        }
        return config.label;
    }

    private String resolveAttackLabel(int stage) {
        if (config.attackLabels != null && stage < config.attackLabels.length) {
            return config.attackLabels[stage];
        }
        return config.label;
    }

    private String resolveGrowthLabel(int fromStage) {
        if (config.growthLabels != null && fromStage < config.growthLabels.length) {
            return config.growthLabels[fromStage];
        }
        return null;
    }
}
