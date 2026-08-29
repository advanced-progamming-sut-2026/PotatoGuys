package com.pvz.controller.game;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.pvz.PvZ2;
import com.pvz.controller.game.State.Playing;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.Hitbox;
import com.pvz.models.entities.LawnMower;
import com.pvz.models.entities.effects.Effect;
import com.pvz.models.entities.effects.LootDrop;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.games.effects.ChapterEffect;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.modes.variants.IZombieMode;
import com.pvz.view.PamActor;
import com.pvz.view.PlantData;

public class GameRenderer {
    private GameContext ctx;
    private SpriteBatch batch;
    private TextureRegion[] backgroundTextures;
    private GameController controller;
    private float previewStateTime;
    private float stateTime;

    /** Debug: draws every entity's hitbox rectangle (toggle with F1). */
    private boolean showHitboxes = true;

    /**
     * When true, ZOMBIE-owned suns are not drawn (host side of networked IZombie).
     */
    private boolean hideZombieSuns;

    /**
     * When set (network guest), draw() renders these remote frames directly instead
     * of reconstructing entities locally. The guest's ctx entity lists are empty.
     */
    private java.util.List<FrameConfig> remoteFrames;
    private IZombieMode remoteFramesIzMode;

    public void setRemoteFrame(java.util.List<FrameConfig> frames, IZombieMode izMode) {
        this.remoteFrames = frames;
        this.remoteFramesIzMode = izMode;
    }

    public GameRenderer(GameContext ctx, GameController controller, SpriteBatch batch,
            TextureRegion[] backgroundTextures) {
        this.ctx = ctx;
        this.batch = batch;
        this.backgroundTextures = backgroundTextures;
        this.controller = controller;
    }

    public void setContext(GameContext ctx) {
        this.ctx = ctx;
    }

    public void setHideZombieSuns(boolean hide) {
        this.hideZombieSuns = hide;
    }

    public void draw() {
        // Network guest: draw the host's rendered frames directly, no local entity
        // simulation. Background + I,Zombie HUD overlays stay local.
        if (remoteFrames != null) {
            drawRemoteFrames(remoteFrames, remoteFramesIzMode);
            drawDebugShapes();
            return;
        }
        batch.setProjectionMatrix(controller.getCamera().combined);
        batch.begin();
        drawBackground();
        drawDisplayZombies();
        if (ctx != null) {
            int totalLanes = ctx.getMap().getLanes();
            drawChapterEffects();
            for (int lane = -1; lane < totalLanes; lane++) {
                drawInactiveLawnMowers(lane);
                drawPlantFoodFx(lane);
                drawPlants(lane);
                drawTileBehaviors(lane);
                drawZombies(lane);
                drawProjectiles(lane);
                drawOctopusProjectiles(lane);
                drawEffects(lane);
                drawActiveLawnMowers(lane);
            }
            drawProjectilesOutsideGrid();
            drawSuns();
            drawLootDrops();
            drawPlacementPreview();
            drawIZombieOverlay();
        }
        batch.end();

        if (controller.getState() instanceof Playing && controller.getGameUiModal() != null && ctx != null) {
            boolean hasSelectedCard = controller.getGameUiModal().getSelectedCard() != null
                    || controller.getGameUiModal().getSelectedZombieCard() != null;
            boolean shovelArmed = controller.getGameUiModal().isShovelSelected();
            boolean plantFoodArmed = controller.getGameUiModal().isPlantFoodSelected();
            if (hasSelectedCard || shovelArmed || plantFoodArmed) {
                controller.getTouchPos().set(Gdx.input.getX(), Gdx.input.getY(), 0);
                controller.getViewport().unproject(controller.getTouchPos());

                Tile hoveredTile = ctx.getMap().getTileAt(controller.getTouchPos().x, controller.getTouchPos().y);

                if (hoveredTile != null) {
                    drawPlacementHighlights(hoveredTile);
                }
            }
        }
        drawDebugShapes();
    }

    private void drawBackground() {
        float x = -backgroundTextures[0].getRegionWidth();
        batch.draw(backgroundTextures[0], x, 0);
        batch.draw(backgroundTextures[1], 0, controller.getBackgroundYOffset());
        batch.draw(backgroundTextures[2], backgroundTextures[1].getRegionWidth(), 0);
    }

    private void drawChapterEffects() {
        for (ChapterEffect ce : ctx.getActiveEffects()) {
            drawFrames(ce.draw());
        }
    }

    /**
     * Draws the decorative wave-1 zombies that stand on the far right during the
     * intro camera pan (before the real GameContext exists). They always loop
     * their idle clip; they use the renderer's shared {@link #stateTime}, so
     * there is no per-frame lag.
     */
    private void drawDisplayZombies() {
        List<DisplayZombie> zombies = controller.getDisplayZombies();
        if (zombies == null || zombies.isEmpty())
            return;
        for (DisplayZombie dz : zombies) {
            PvZ2.pamPlayer.draw(batch, dz.pamPath, dz.idleClip, stateTime,
                    dz.x, dz.y, dz.scale, dz.scale, true);
        }
    }

    private void drawInactiveLawnMowers(int row) {
        if (row < 0 || ctx.getLawnMowers() == null || ctx.getLawnMowers().length < 1) {
            return;
        }
        LawnMower lm = ctx.getLawnMowers()[row];
        if (lm != null && !lm.isTriggered()) {
            drawFrames(lm.draw());
        }
    }

    private void drawPlantFoodFx(int row) {
        for (Effect e : ctx.getEffects()) {
            if (!(e instanceof com.pvz.models.entities.effects.PlantFoodFxEffect)) {
                continue;
            }
            int lane = Math.max(0, Math.min(GameController.worldYtoLane(e.getPos().y),
                    ctx.getMap().getLanes() - 1));
            if (lane == row) {
                drawFrames(e.draw());
            }
        }
    }

    private void drawPlants(int row) {
        for (Plant p : ctx.getPlants()) {
            if (p.getLane() == row && !p.isBound()) {
                drawFrames(p.draw());
            }
        }
    }

    private void drawTileBehaviors(int lane) {
        if (lane < 0) {
            return;
        }
        for (int col = 0; col < ctx.getMap().getColumns(); col++) {
            List<FrameConfig> frames = ctx.getMap().getTileAt(col, lane).drawBehaviors();
            drawFrames(frames);
        }
    }

    private static final String ICE_BLOCK_PAM = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    private static final String ICE_BLOCK_CLIP = "freeze_idle";

    private void drawZombies(int row) {
        for (Zombie z : ctx.getZombies()) {
            if (GameController.worldYtoLane(z.getY()) == row) {
                for (FrameConfig fc : z.draw()) {
                    if (fc != null) {
                        batch.setColor(fc.r, fc.g, fc.b, fc.a);
                        drawFrame(fc);
                        batch.setColor(1f, 1f, 1f, 1f);
                    }
                    if (z.isFrozenInIceBlock()) {
                        z.addIceBlockTime(1f / 60f);
                        batch.setColor(1f, 1f, 1f, 0.7f);
                        PvZ2.pamPlayer.draw(batch, ICE_BLOCK_PAM, ICE_BLOCK_CLIP,
                                z.getIceBlockStateTime(), z.getX(), z.getY(),
                                0.65f, 0.65f, false);
                        batch.setColor(1f, 1f, 1f, 1f);
                    }
                }
            }
        }
    }

    private void drawProjectiles(int row) {
        for (Projectile p : ctx.getProjectiles()) {
            int pLane = GameController.worldYtoLane(p.getY());
            if (pLane == row) {
                drawFrames(p.draw());
            }
        }
    }

    private void drawProjectilesOutsideGrid() {
        int totalLanes = ctx.getMap().getLanes();
        for (Projectile p : ctx.getProjectiles()) {
            int pLane = GameController.worldYtoLane(p.getY());
            if (pLane < 0 || pLane >= totalLanes) {
                drawFrames(p.draw());
            }
        }
        for (var op : ctx.getOctopusProjectiles()) {
            int opLane = GameController.worldYtoLane(op.getPosition().y);
            if (opLane < 0 || opLane >= totalLanes) {
                drawFrames(op.draw());
            }
        }
    }

    private void drawOctopusProjectiles(int row) {
        for (var op : ctx.getOctopusProjectiles()) {
            int opLane = GameController.worldYtoLane(op.getPosition().y);
            if (opLane == row) {
                drawFrames(op.draw());
            }
        }
    }

    private void drawEffects(int row) {
        for (Effect e : ctx.getEffects()) {
            // Plant-food shine is drawn behind the plants by drawPlantFoodFx().
            if (e instanceof com.pvz.models.entities.effects.PlantFoodFxEffect) {
                continue;
            }
            int lane = Math.max(-1, Math.min(GameController.worldYtoLane(e.getPos().y),
                    ctx.getMap().getLanes() - 1));
            if (lane == row) {
                drawFrames(e.draw());
            }
        }
    }

    private void drawSuns() {
        for (Sun s : ctx.getSuns()) {
            if (hideZombieSuns && s.getOwner() == Sun.SunOwner.ZOMBIE)
                continue;
            drawFrames(s.draw());
        }
    }

    private void drawLootDrops() {
        for (LootDrop ld : ctx.getLootDrops()) {
            drawFrames(ld.draw());
        }
    }

    private void drawActiveLawnMowers(int row) {
        if (row < 0 || ctx.getLawnMowers() == null || ctx.getLawnMowers().length < 1) {
            return;
        }
        LawnMower lm = ctx.getLawnMowers()[row];
        if (lm != null && lm.isTriggered()) {
            drawFrames(lm.draw());
        }
    }

    /**
     * Draws the "ghost" of the currently selected seed packet following the mouse:
     * while a plant card is armed, the plant's idle PAM animation is played under
     * the
     * cursor, semi-transparent, until the plant is placed on a tile (or
     * deselected).
     */
    private void drawPlacementPreview() {
        if (!(controller.getState() instanceof Playing) || controller.isPaused())
            return;

        controller.getTouchPos().set(Gdx.input.getX(), Gdx.input.getY(), 0);
        controller.getViewport().unproject(controller.getTouchPos());

        // Plant preview
        if (controller.getGameUiModal() != null && controller.getGameUiModal().getSelectedCard() != null) {
            PlantCard card = controller.getGameUiModal().getSelectedCard();
            if (card != controller.getPreviewPlantCard()) {
                controller.setPreviewPlantCard(card);
                previewStateTime = 0f;
            }
            PlantData data = PlantData.forType(card.getPlant().getType());
            if (data != null) {
                String pamPath = data.pamPath();
                String idleLabel = data.idleLabel();
                if (pamPath != null) {
                    batch.setColor(1f, 1f, 1f, 0.90f);
                    PvZ2.pamPlayer.draw(batch, pamPath, idleLabel, previewStateTime,
                            controller.getTouchPos().x, controller.getTouchPos().y, 0.7f, 0.7f, true);
                    batch.setColor(Color.WHITE);
                }
            }
            return;
        }

        // Zombie preview
        if (controller.getGameUiModal() != null && controller.getGameUiModal().getSelectedZombieCard() != null) {
            ZombieCard zCard = controller.getGameUiModal().getSelectedZombieCard();
            if (zCard != controller.getPreviewZombieCard()) {
                controller.setPreviewZombieCard(zCard);
                previewStateTime = 0f;
            }
            com.pvz.models.entities.zombies.data.ZombiePropertySheet sheet = com.pvz.models.entities.zombies.data.ZombieRegistry
                    .getInstance().getSheet(
                            zCard.getZombieType().getAlias());
            if (sheet != null && sheet.getAnimationConfig() != null) {
                String pamPath = sheet.getAnimationConfig().pamFilePath;
                String idleLabel = sheet.getAnimationConfig().idleLabel;
                float scale = sheet.getAnimationConfig().scale != null ? sheet.getAnimationConfig().scale : 0.65f;
                if (pamPath != null && idleLabel != null) {
                    java.util.Map<String, Boolean> partsVisibility = buildPreviewPartsVisibility(sheet);
                    batch.setColor(1f, 1f, 1f, 0.90f);
                    if (partsVisibility != null) {
                        PamActor.drawWithVisibility(batch, pamPath, idleLabel, previewStateTime,
                                controller.getTouchPos().x, controller.getTouchPos().y, scale, true, partsVisibility);
                    } else {
                        PvZ2.pamPlayer.draw(batch, pamPath, idleLabel, previewStateTime,
                                controller.getTouchPos().x, controller.getTouchPos().y, scale, scale, true);
                    }
                    batch.setColor(Color.WHITE);
                }
            }
        }
    }

    private java.util.Map<String, Boolean> buildPreviewPartsVisibility(
            com.pvz.models.entities.zombies.data.ZombiePropertySheet sheet) {
        java.util.Map<String, Boolean> visibility = null;
        for (String armorAlias : sheet.getArmorAliases()) {
            String cleanAlias = armorAlias.contains(":")
                    ? armorAlias.substring(armorAlias.indexOf(':') + 1)
                    : armorAlias;
            com.pvz.models.entities.zombies.data.ArmorPropertySheet aSheet = com.pvz.models.entities.zombies.data.ZombieRegistry
                    .getInstance().getArmorSheet(cleanAlias);
            if (aSheet == null)
                continue;
            com.pvz.models.entities.zombies.armor.ArmorType type = com.pvz.models.entities.zombies.armor.ArmorType
                    .fromString(aSheet.getArmorType());
            String[] layers = type.pamLayers();
            if (layers == null)
                continue;
            if (visibility == null)
                visibility = new java.util.HashMap<>();
            for (int i = 0; i < layers.length; i++) {
                visibility.put(layers[i], i == 0);
            }
            String container = type.pamContainerName();
            if (container != null)
                visibility.put(container, true);
            for (String alive : type.pamAliveParts())
                visibility.put(alive, true);
            for (String crit : type.pamCriticalParts())
                visibility.put(crit, false);
        }
        return visibility;
    }

    /**
     * Highlights the whole row (lane) and whole column under the hovered tile in
     * white,
     * matching the reference implementation's placement preview: while a plant card
     * is
     * armed, hovering a tile paints its entire lane and column so the player sees
     * exactly
     * which row/column the plant will land in.
     */
    private void drawPlacementHighlights(Tile tile) {
        GameMap map = ctx.getMap();
        int lanes = map.getLanes();
        int cols = map.getColumns();

        float boardWidth = cols * GameMap.TILE_WIDTH;
        float gridBottom = GameMap.TOP_LANE_Y - (lanes - 1) * GameMap.TILE_HEIGHT;

        float rowY = tile.getY();
        float colX = tile.getX();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        controller.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
        controller.getShapeRenderer().setColor(1f, 1f, 1f, 0.40f);
        controller.getShapeRenderer().rect(GameMap.START_X, rowY, boardWidth, GameMap.TILE_HEIGHT);
        controller.getShapeRenderer().rect(colX, gridBottom, GameMap.TILE_WIDTH, lanes * GameMap.TILE_HEIGHT);
        controller.getShapeRenderer().end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawIZombieOverlay() {
        if (!(ctx.getMode() instanceof IZombieMode izMode))
            return;

        int lanes = ctx.getMap().getLanes();

        // Red line at the placement boundary (shapeRenderer)
        batch.end();
        float redLineX = GameController.colToWorldX(izMode.getRedLineColumn()) - GameMap.TILE_WIDTH / 2f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        controller.getShapeRenderer().setProjectionMatrix(controller.getCamera().combined);
        controller.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
        controller.getShapeRenderer().setColor(Color.RED);
        controller.getShapeRenderer().rect(redLineX, GameMap.TOP_LANE_Y - (lanes - 1) * GameMap.TILE_HEIGHT, 4f,
                lanes * GameMap.TILE_HEIGHT);
        controller.getShapeRenderer().end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.setProjectionMatrix(controller.getCamera().combined);
        batch.begin();

        // Brain indicators at column -1 for each lane (off-screen, like lawn mowers)
        com.badlogic.gdx.graphics.g2d.TextureRegion brainRegion = PvZ2.textureBank
                .region("IMAGE_UI_CURRENCY_VALENBRAINZ_STACK_0");
        boolean[] brainsEaten = izMode.getBrainsEaten();
        if (brainsEaten == null)
            return;
        float brainX = GameController.colToWorldX(-1);
        float brainSize = 65f;
        for (int lane = 0; lane < lanes; lane++) {
            if (lane < brainsEaten.length && !brainsEaten[lane]) {
                float brainY = GameController.laneToWorldY(lane);
                if (brainRegion != null) {
                    batch.draw(brainRegion, brainX - brainSize / 2f, brainY - brainSize / 2f, brainSize, brainSize);
                } else {
                    batch.end();
                    controller.getShapeRenderer().setProjectionMatrix(controller.getCamera().combined);
                    controller.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
                    controller.getShapeRenderer().setColor(Color.MAGENTA);
                    controller.getShapeRenderer().circle(brainX, brainY, 18f);
                    controller.getShapeRenderer().end();
                    batch.setProjectionMatrix(controller.getCamera().combined);
                    batch.begin();
                }
            }
        }

        // Sun icon above sun-producer zombies
        List<Zombie> sunProducers = izMode.getSunProducers();
        float bob = 4f * (float) Math.sin(stateTime * 2.0f);
        float sunScale = 0.45f;
        for (Zombie z : sunProducers) {
            if (z.isDead())
                continue;
            PvZ2.pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", "animation",
                    stateTime, z.getX(), z.getY() + 55f + bob, sunScale, sunScale, true);
        }
    }

    private void drawDebugShapes() {
        controller.getShapeRenderer().setProjectionMatrix(controller.getCamera().combined);
        // ۶. رسم خورشیدها و خطوط دیباگ گرید
        if (ctx != null) {
            controller.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);

            // رسم خورشیدها
            controller.getShapeRenderer().setColor(Color.YELLOW);
            /*
             * for (Sun sun : new ArrayList<>(context.getSuns())) {
             * if (!sun.isDone()) {
             * shapeRenderer.circle(sun.getX(), sun.getY(), 50);
             * }
             * }
             */
            // رسم خطوط گرید دیباگ
            boolean showGrid = false;
            try {
                var user = com.pvz.models.AppContext.getInstance().getCurrentUser();
                if (user != null)
                    showGrid = user.getSetting().isShowGrid();
            } catch (Exception ignored) {
            }
            if (showGrid) {
                controller.getShapeRenderer().end();
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                controller.getShapeRenderer().setProjectionMatrix(controller.getCamera().combined);
                controller.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
                controller.getShapeRenderer().setColor(1f, 0f, 0f, 0.4f);
                int lanes = ctx.getMap().getLanes();
                int cols = ctx.getMap().getColumns();
                float boardWidth = cols * GameMap.TILE_WIDTH;
                float gridBottom = GameMap.TOP_LANE_Y - (lanes - 1) * GameMap.TILE_HEIGHT;
                float gridHeight = lanes * GameMap.TILE_HEIGHT;
                for (int i = 0; i <= lanes; i++) {
                    float y = GameMap.TOP_LANE_Y + GameMap.TILE_HEIGHT - i * GameMap.TILE_HEIGHT;
                    controller.getShapeRenderer().rect(GameMap.START_X, y - 1f, boardWidth, 2f);
                }
                for (int i = 0; i <= cols; i++) {
                    float x = GameMap.START_X + i * GameMap.TILE_WIDTH;
                    controller.getShapeRenderer().rect(x - 1f, gridBottom, 2f, gridHeight);
                }
                controller.getShapeRenderer().end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
                controller.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
            }

            // for(Plant a : ctx.getPlants()){
            // shapeRenderer.circle(GameController.xToWorldX(a.getCol()),
            // GameController.yToWorldY(a.getLane()), 10);
            // }

            // for(Projectile a : ctx.getProjectiles()){
            // shapeRenderer.circle(a.getX(), a.getY(), 10);
            // }

            // for(Zombie a : ctx.getZombies()){
            // shapeRenderer.circle(a.getX(),a.getY(), 10);
            // }

            controller.getShapeRenderer().end();

            // Hitbox debug (outline)
            controller.getShapeRenderer().begin(ShapeRenderer.ShapeType.Line);
            drawHitboxes();
            controller.getShapeRenderer().end();
        }
    }

    // ── Hitbox debug ─────────────────────────────────────────────────────────

    private void drawHitboxes() {
        if (!showHitboxes || !com.pvz.utils.DebugMode.isEnabled())
            return;

        drawEntityHitboxes(Color.GREEN, ctx.getPlants());
        drawEntityHitboxes(Color.RED, ctx.getZombies());
        drawEntityHitboxes(Color.CYAN, ctx.getProjectiles());
        drawEntityHitboxes(Color.YELLOW, ctx.getSuns());
        for (LawnMower m : ctx.getLawnMowers()) {
            if (m != null)
                drawEntityHitbox(Color.ORANGE, m);
        }
    }

    private void drawEntityHitboxes(Color color, List<? extends Entity> entities) {
        for (Entity e : entities) {
            drawEntityHitbox(color, e);
        }
    }

    private void drawEntityHitbox(Color color, Entity e) {
        Hitbox hitbox = e.getHitbox();
        if (hitbox == null)
            return;
        Rectangle r = hitbox.getRectangle();
        controller.getShapeRenderer().setColor(color);
        controller.getShapeRenderer().rect(r.x, r.y, r.width, r.height);
    }

    private void drawFrames(List<FrameConfig> frameConfigs) {
        if (frameConfigs == null) {
            return;
        }

        for (FrameConfig fc : frameConfigs) {
            drawFrame(fc);
        }
    }

    private void drawFrame(FrameConfig frameConfig) {
        if (frameConfig == null) {
            return;
        }
        batch.setColor(frameConfig.r, frameConfig.g, frameConfig.b, frameConfig.a);
        if (frameConfig.partsVisibility == null) {
            PvZ2.pamPlayer.draw(batch, frameConfig.pamPath, frameConfig.label,
                    frameConfig.stateTime, frameConfig.position.x, frameConfig.position.y,
                    frameConfig.scale.x, frameConfig.scale.y, frameConfig.looping);
        } else {
            PamActor.drawWithVisibility(batch, frameConfig.pamPath, frameConfig.label,
                    frameConfig.stateTime, frameConfig.position.x, frameConfig.position.y,
                    frameConfig.scale.x, frameConfig.looping, frameConfig.partsVisibility);
        }
        batch.setColor(Color.WHITE);
    }

    public void update(float dt) {
        previewStateTime += dt;
        stateTime += dt;
    }

    /**
     * Drawn by the guest instead of the entity-based {@link #draw()} body: renders
     * the exact list of frames the host sent (see {@link #collectFrames()}) in
     * order,
     * on top of the local background, plus the locally-rendered I,Zombie HUD
     * scalars
     * (brains / red line). Background and non-PAM overlays stay local.
     */
    public void drawRemoteFrames(List<FrameConfig> frames, IZombieMode izMode) {
        batch.setProjectionMatrix(controller.getCamera().combined);
        batch.begin();
        drawBackground();
        if (frames != null) {
            for (FrameConfig fc : frames) {
                drawFrame(fc);
            }
        }
        // The guest's own collectible zombie suns are minted locally (not part of the
        // host's frame), so draw them on top of the remote picture.
        if (ctx != null) {
            drawSuns();
        }
        drawIZombieOverlayLocal(izMode);
        batch.end();
    }

    private void drawIZombieOverlayLocal(IZombieMode izMode) {
        if (izMode == null || ctx == null)
            return;
        drawIZombieOverlay();
    }

    /**
     * Collects every {@link FrameConfig} that {@link #draw()} would render, in the
     * exact same draw order, without touching the batch. Used by the networking
     * layer to ship the host's rendered picture to the guest (see
     * {@link com.pvz.network.game.RenderFrame}).
     *
     * <p>
     * Elements that aren't PAM frames (the background textures, the I,Zombie red
     * line and brain indicators) are not included here: they're drawn locally on
     * the
     * guest with its own synced scalars (brainsEaten / plantSurvivalSeconds). Only
     * the
     * actual animated sprite frames need to cross the wire.
     */
    public List<FrameConfig> collectFrames() {
        List<FrameConfig> frames = new java.util.ArrayList<>();
        if (ctx == null)
            return frames;

        int totalLanes = ctx.getMap().getLanes();
        for (int lane = 0; lane < totalLanes; lane++) {
            collectInactiveLawnMowers(lane, frames);
            collectPlants(lane, frames);
            collectTileBehaviors(lane, frames);
            collectZombies(lane, frames);
            collectProjectiles(lane, frames);
            collectOctopusProjectiles(lane, frames);
            collectEffects(lane, frames);
            collectActiveLawnMowers(lane, frames);
        }
        collectProjectilesOutsideGrid(frames);
        collectSuns(frames);
        collectLootDrops(frames);
        return frames;
    }

    private static void addFrames(List<FrameConfig> frames, java.util.List<FrameConfig> more) {
        if (more == null)
            return;
        for (FrameConfig fc : more) {
            if (fc != null)
                frames.add(fc);
        }
    }

    private void collectInactiveLawnMowers(int row, List<FrameConfig> frames) {
        if (ctx.getLawnMowers() == null || ctx.getLawnMowers().length < 1)
            return;
        LawnMower lm = ctx.getLawnMowers()[row];
        if (lm != null && !lm.isTriggered())
            addFrames(frames, lm.draw());
    }

    private void collectPlants(int row, List<FrameConfig> frames) {
        for (Plant p : ctx.getPlants()) {
            if (p.getLane() == row && !p.isBound())
                addFrames(frames, p.draw());
        }
    }

    private void collectTileBehaviors(int row, List<FrameConfig> frames) {
        for (int col = 0; col < ctx.getMap().getColumns(); col++) {
            addFrames(frames, ctx.getMap().getTileAt(col, row).drawBehaviors());
        }
    }

    private void collectZombies(int row, List<FrameConfig> frames) {
        for (Zombie z : ctx.getZombies()) {
            if (GameController.worldYtoLane(z.getY()) == row) {
                addFrames(frames, z.draw());
                if (z.isFrozenInIceBlock()) {
                    frames.add(new FrameConfig(ICE_BLOCK_PAM, ICE_BLOCK_CLIP,
                            z.getIceBlockStateTime(), new com.badlogic.gdx.math.Vector2(z.getX(), z.getY()),
                            new com.badlogic.gdx.math.Vector2(0.65f, 0.65f), null, false));
                }
            }
        }
    }

    private void collectProjectiles(int row, List<FrameConfig> frames) {
        for (Projectile p : ctx.getProjectiles()) {
            int pLane = GameController.worldYtoLane(p.getY());
            if (pLane == row)
                addFrames(frames, p.draw());
        }
    }

    private void collectOctopusProjectiles(int row, List<FrameConfig> frames) {
        for (var op : ctx.getOctopusProjectiles()) {
            int opLane = GameController.worldYtoLane(op.getPosition().y);
            if (opLane == row)
                frames.addAll(op.draw());
        }
    }

    private void collectProjectilesOutsideGrid(List<FrameConfig> frames) {
        int totalLanes = ctx.getMap().getLanes();
        for (Projectile p : ctx.getProjectiles()) {
            int pLane = GameController.worldYtoLane(p.getY());
            if (pLane < 0 || pLane >= totalLanes)
                addFrames(frames, p.draw());
        }
        for (var op : ctx.getOctopusProjectiles()) {
            int opLane = GameController.worldYtoLane(op.getPosition().y);
            if (opLane < 0 || opLane >= totalLanes)
                frames.addAll(op.draw());
        }
    }

    private void collectEffects(int row, List<FrameConfig> frames) {
        for (Effect e : ctx.getEffects()) {
            int lane = Math.max(0, Math.min(GameController.worldYtoLane(e.getPos().y),
                    ctx.getMap().getLanes() - 1));
            if (lane == row)
                addFrames(frames, e.draw());
        }
    }

    private void collectSuns(List<FrameConfig> frames) {
        for (Sun s : ctx.getSuns()) {
            if (hideZombieSuns && s.getOwner() == Sun.SunOwner.ZOMBIE)
                continue;
            addFrames(frames, s.draw());
        }
    }

    private void collectLootDrops(List<FrameConfig> frames) {
        for (LootDrop ld : ctx.getLootDrops()) {
            addFrames(frames, ld.draw());
        }
    }

    private void collectActiveLawnMowers(int row, List<FrameConfig> frames) {
        if (ctx.getLawnMowers() == null || ctx.getLawnMowers().length < 1)
            return;
        LawnMower lm = ctx.getLawnMowers()[row];
        if (lm != null && lm.isTriggered())
            addFrames(frames, lm.draw());
    }

    public void toggleShowHitBoxes() {
        showHitboxes = !showHitboxes;
    }
}
