package com.pvz.models.games.map.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

/**
 * TileBehavior representing an octopus overlay placed on a plant by the
 * Octopus Zombie. While active:
 * <ul>
 *   <li>The covered plant is disabled ({@code bound} = true) — it cannot
 *       update, attack, produce sun, or respond to Plant Food.</li>
 *   <li>New plants cannot be planted on this tile.</li>
 *   <li>Projectiles hitting this tile are absorbed by the octopus (not
 *       the plant underneath).</li>
 *   <li>The octopus has independent HP and can be destroyed.</li>
 * </ul>
 *
 * <p>When the octopus HP reaches zero, it removes itself, frees the tile,
 * and re-enables the covered plant.</p>
 *
 * <p><b>Shovel interaction:</b> if the shovel system checks behaviors, the
 * octopus should be removed before the plant. This is handled by the
 * tile's {@code isPlantable} and behavior lifecycle: removing the
 * behavior frees the tile.</p>
 */
public class OctopusOverlayBehavior implements TileBehavior {

    private static final String PAM_PATH =
        "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM";
    private static final String LAND_CLIP = "animation2";
    private static final String LOOP_CLIP = "animation3";
    private static final float LAND_DURATION = 1.0f;
    private static final float FLASH_DURATION = 0.28f;

    private final GameContext ctx;
    private final Tile tile;
    private final float maxHp;
    private float hp;
    private float stateTime = 0f;
    private float flashTimer;

    private Plant boundPlant;
    private final int col;
    private final int lane;

    public OctopusOverlayBehavior(GameContext ctx, Tile tile, float hp,
                                  int col, int lane) {
        this.ctx = ctx;
        this.tile = tile;
        this.hp = hp;
        this.maxHp = hp;
        this.col = col;
        this.lane = lane;
    }

    /** The plant this overlay is covering (may be null if plant died mid-flight). */
    public void setBoundPlant(Plant plant) {
        this.boundPlant = plant;
    }

    public Plant getBoundPlant() {
        return boundPlant;
    }

    // ── TileBehavior overrides ──────────────────────────────────────────────

    /**
     * Projectiles hitting this tile are absorbed by the octopus overlay
     * instead of passing through to the plant underneath.
     */
    @Override
    public void onProjectileHit(Projectile p, Tile tile) {
        hp -= p.getDamage();
        flashTimer = FLASH_DURATION;
        p.destroy();
        if (hp <= 0f) {
            destroy();
        }
    }

    /**
     * Raw damage (e.g. from Jalapeno, Cherry Bomb) also damages the overlay.
     */
    @Override
    public void processHit(float damage) {
        hp -= damage;
        flashTimer = FLASH_DURATION;
        if (hp <= 0f) {
            destroy();
        }
    }

    /** No plants can be planted on a tile occupied by an octopus overlay. */
    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
        return false;
    }

    @Override
    public void update(GameContext ctx, Tile tile, float dt) {
        stateTime += dt;
        if (flashTimer > 0f) flashTimer = Math.max(0f, flashTimer - dt);
    }

    @Override
    public String getName() {
        return "OctopusOverlay";
    }

    @Override
    public String getStatus() {
        return "\n    Octopus HP: " + String.format("%.0f", hp);
    }

    @Override
    public FrameConfig draw(Tile tile) {
        Vector2 pos = new Vector2(
            GameController.colToWorldX(tile.getCol()),
            GameController.laneToWorldY(tile.getLane()));
        Vector2 scale = new Vector2(0.7f, 0.7f);
        boolean looping;
        float animTime;
        if (stateTime < LAND_DURATION) {
            looping = false;
            animTime = stateTime;
        } else {
            looping = true;
            animTime = stateTime - LAND_DURATION;
        }
        String clip = (stateTime < LAND_DURATION) ? LAND_CLIP : LOOP_CLIP;
        FrameConfig fc = new FrameConfig(PAM_PATH, clip, animTime,
            pos, scale, null, looping);
        if (flashTimer > 0f) {
            fc.setColor(5f, 5f, 5f, 0.6f);
        }
        return fc;
    }

    // ── Destruction ─────────────────────────────────────────────────────────

    /**
     * Destroys the octopus overlay: removes the behavior and tag from the
     * tile, and re-enables the bound plant if it still exists.
     */
    public void destroy() {
        tile.removeBehavior(this);
        tile.getTags().remove(TileTags.OCTOPUS);

        if (boundPlant != null && !boundPlant.isDead()) {
            boundPlant.setBound(false);
            ctx.log("[Octopus] Plant at (" + col + "," + lane + ") is free!");
        }

        ctx.log("[Octopus] Overlay destroyed on tile (" + col + "," + lane + ")");
    }

    /**
     * Instant-kill the overlay (e.g. from an instant-kill projectile
     * or a plant food effect).
     */
    public void instantKill() {
        hp = 0f;
        destroy();
    }
}
