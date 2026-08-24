package com.pvz.models.entities.projectile;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.projectile.fsm.LobbedMotionState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.OctopusOverlayBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

/**
 * A parabolic octopus projectile thrown by the Octopus Zombie toward a
 * target plant. Flies in an arc from the zombie's position to the
 * target tile. On landing:
 * <ul>
 * <li>If a plant exists on the tile: the plant is bound (disabled)
 * and an {@link OctopusOverlayBehavior} is added to the tile.</li>
 * <li>If no plant exists: the overlay still lands and blocks the tile.</li>
 * </ul>
 *
 * <p>
 * Follows the same parabolic math as {@link LobbedMotionState} but is
 * a standalone entity (not a regular {@link Projectile}) since it targets
 * plants rather than zombies.
 */
public class OctopusProjectile extends Entity {

    private static final String PAM_PATH = "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM";
    private static final String CLIP = "animation";

    private final GameContext ctx;
    private final Vector2 startPos = new Vector2();
    private final Vector2 targetPos = new Vector2();
    private final int targetCol;
    private final int targetLane;
    private final float octopusHp;
    private final float flightSeconds;
    private final float arcHeight;

    private float stateTime = 0f;
    private boolean landed = false;

    public OctopusProjectile(GameContext ctx,
            float startX, float startY,
            float targetX, float targetY,
            int targetCol, int targetLane,
            float octopusHp,
            float flightSeconds, float arcHeight) {
        this.ctx = ctx;
        this.startPos.set(startX, startY);
        this.targetPos.set(targetX, targetY);
        this.targetCol = targetCol;
        this.targetLane = targetLane;
        this.octopusHp = octopusHp;
        this.flightSeconds = Math.max(0.1f, flightSeconds);
        this.arcHeight = arcHeight;
        position.set(startX, startY);
    }

    @Override
    public void enter() {
        stateTime = 0f;
        landed = false;
    }

    @Override
    public void update(float dt) {
        if (landed)
            return;
        stateTime += dt;
        float t = Math.min(1f, stateTime / flightSeconds);

        float x = startPos.x + (targetPos.x - startPos.x) * t;
        float baseY = startPos.y + (targetPos.y - startPos.y) * t;
        float arcOffset = arcHeight * (float) Math.sin(t * Math.PI);
        position.set(x, baseY + arcOffset);

        if (t >= 1f) {
            landed = true;
            onLand();
            dispose();
        }
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        frameConfigs.add(new FrameConfig(PAM_PATH, CLIP, stateTime,
                new Vector2(position.x, position.y),
                new Vector2(0.75f, 0.75f), null, true));
        return frameConfigs;
    }

    @Override
    public void dispose() {
        ctx.removeOctopusProjectile(this);
    }

    /**
     * Called when the projectile reaches its target tile. Spawns an
     * {@link OctopusOverlayBehavior} on the tile. If a plant is present,
     * it is bound (disabled). If the plant was destroyed mid-flight, the
     * octopus still lands and blocks the tile.
     */
    private void onLand() {
        Tile tile = ctx.getTileAt(targetCol, targetLane);
        if (tile == null)
            return;

        // If the tile already has an octopus overlay, don't stack another one
        for (var b : tile.getBehaviors()) {
            if (b instanceof OctopusOverlayBehavior)
                return;
        }

        OctopusOverlayBehavior overlay = new OctopusOverlayBehavior(
                ctx, tile, octopusHp, targetCol, targetLane);
        tile.addBehavior(overlay);
        tile.getTags().add(TileTags.OCTOPUS);

        // Bind the plant if one exists on this tile
        if (!tile.getPlants().isEmpty()) {
            for (var plant : tile.getPlants()) {
                if (!plant.isDead()) {
                    plant.setBound(true);
                    overlay.setBoundPlant(plant);
                    break;
                }
            }
        }

        ctx.log("[Octopus] Octopus landed on tile (" + targetCol + "," + targetLane + ")");
    }
}
