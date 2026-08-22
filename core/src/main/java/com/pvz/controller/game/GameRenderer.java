package com.pvz.controller.game;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.pvz.PvZ2;
import com.pvz.controller.game.State.Playing;
import com.pvz.models.engine.FrameConfig;
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

    public GameRenderer(GameContext ctx, GameController controller, SpriteBatch batch,
            TextureRegion[] backgroundTextures) {
        this.ctx = ctx;
        this.batch = batch;
        this.backgroundTextures = backgroundTextures;
    }

    public void draw() {
        batch.setProjectionMatrix(controller.getCamera().combined);
        batch.begin();
        drawBackground();
        if (ctx != null) {
            int totalLanes = ctx.getMap().getLanes();
            for (int lane = 0; lane < totalLanes; lane++) {
                drawInactiveLawnMowers(lane);
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
    }

    private void drawBackground() {
        float x = -backgroundTextures[0].getRegionWidth();
        batch.draw(backgroundTextures[0], x, 0);
        batch.draw(backgroundTextures[1], 0, 0);
        batch.draw(backgroundTextures[2], backgroundTextures[1].getRegionWidth(), 0);
    }

    private void drawInactiveLawnMowers(int row) {
        if (ctx.getLawnMowers() == null || ctx.getLawnMowers().length < 1) {
            return;
        }
        LawnMower lm = ctx.getLawnMowers()[row];
        if (lm != null && !lm.isTriggered()) {
            FrameConfig fc = lm.draw();
            PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                    fc.stateTime, fc.position.x, fc.position.y,
                    fc.scale.x, fc.scale.y, fc.looping);
        }
    }

    private void drawPlants(int row) {
        for (Plant p : ctx.getPlants()) {
            if (p.getLane() == row && !p.isBound()) {
                FrameConfig fc = p.draw();
                if (fc != null) {
                    batch.setColor(fc.r, fc.g, fc.b, fc.a);
                    PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                            fc.stateTime, fc.position.x, fc.position.y,
                            fc.scale.x, fc.scale.y, fc.looping);
                    batch.setColor(1f, 1f, 1f, 1f);
                }
            }
        }
    }

    private void drawTileBehaviors(int row) {
        for (int col = 0; col < ctx.getMap().getColumns(); col++) {
            List<FrameConfig> frames = ctx.getMap().getTileAt(col, row).drawBehaviors();
            if (frames != null) {
                for (FrameConfig fc : frames) {
                    if (fc != null) {
                        batch.setColor(fc.r, fc.g, fc.b, fc.a);
                        PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                                fc.stateTime, fc.position.x, fc.position.y,
                                fc.scale.x, fc.scale.y, fc.looping);
                        batch.setColor(Color.WHITE);
                    }
                }
            }
        }
    }

    private static final String ICE_BLOCK_PAM = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    private static final String ICE_BLOCK_CLIP = "freeze_idle";

    private void drawZombies(int row) {
        for (Zombie z : ctx.getZombies()) {
            if (GameController.worldYtoLane(z.getY()) == row) {
                FrameConfig fc = z.draw();
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

    private void drawProjectiles(int row) {
        for (Projectile p : ctx.getProjectiles()) {
            int pLane = GameController.worldYtoLane(p.getY());
            if (pLane == row) {
                p.draw();
            }
        }
    }

    private void drawProjectilesOutsideGrid() {
        int totalLanes = ctx.getMap().getLanes();
        for (Projectile p : ctx.getProjectiles()) {
            int pLane = GameController.worldYtoLane(p.getY());
            if (pLane < 0 || pLane >= totalLanes) {
                p.draw();
            }
        }
        for (var op : ctx.getOctopusProjectiles()) {
            int opLane = GameController.worldYtoLane(op.getPosition().y);
            if (opLane < 0 || opLane >= totalLanes) {
                FrameConfig fc = op.draw();
                if (fc != null) {
                    PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                            fc.stateTime, fc.position.x, fc.position.y,
                            fc.scale.x, fc.scale.y, fc.looping);
                }
            }
        }
    }

    private void drawOctopusProjectiles(int row) {
        for (var op : ctx.getOctopusProjectiles()) {
            int opLane = GameController.worldYtoLane(op.getPosition().y);
            if (opLane == row) {
                FrameConfig fc = op.draw();
                if (fc != null) {
                    PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                            fc.stateTime, fc.position.x, fc.position.y,
                            fc.scale.x, fc.scale.y, fc.looping);
                }
            }
        }
    }

    private void drawEffects(int row) {
        for (Effect e : ctx.getEffects()) {
            if (GameController.worldYtoLane(e.getPos().y) == row) {
                FrameConfig fc = e.draw();
                if (fc != null) {
                    PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                            fc.stateTime, fc.position.x, fc.position.y,
                            fc.scale.x, fc.scale.y, fc.looping);
                }
            }
        }
    }

    private void drawSuns() {
        for (Sun s : ctx.getSuns()) {
            FrameConfig fc = s.draw();
            if (fc != null) {
                PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                        fc.stateTime, fc.position.x, fc.position.y,
                        fc.scale.x, fc.scale.y, fc.looping);
            }
        }
    }

    private void drawLootDrops() {
        for (LootDrop ld : ctx.getLootDrops()) {
            ld.draw();
        }
    }

    private void drawActiveLawnMowers(int row) {
        if (ctx.getLawnMowers() == null || ctx.getLawnMowers().length < 1) {
            return;
        }
        LawnMower lm = ctx.getLawnMowers()[row];
        if (lm != null && lm.isTriggered()) {
            FrameConfig fc = lm.draw();
            PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                    fc.stateTime, fc.position.x, fc.position.y,
                    fc.scale.x, fc.scale.y, fc.looping);
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
            if (card != previewPlantCard) {
                previewPlantCard = card;
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
            if (zCard != previewZombieCard) {
                previewZombieCard = zCard;
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
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 1f, 0.40f);
        shapeRenderer.rect(GameMap.START_X, rowY, boardWidth, GameMap.TILE_HEIGHT);
        shapeRenderer.rect(colX, gridBottom, GameMap.TILE_WIDTH, lanes * GameMap.TILE_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawIZombieOverlay() {
        if (!(ctx.getMode() instanceof IZombieMode izMode))
            return;

        int lanes = ctx.getMap().getLanes();

        // Red line at the placement boundary (shapeRenderer)
        batch.end();
        float redLineX = colToWorldX(izMode.getRedLineColumn()) - GameMap.TILE_WIDTH / 2f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(redLineX, GameMap.TOP_LANE_Y - (lanes - 1) * GameMap.TILE_HEIGHT, 4f,
                lanes * GameMap.TILE_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Brain indicators at column 0 for each lane
        com.badlogic.gdx.graphics.g2d.TextureRegion brainRegion = PvZ2.textureBank
                .region("IMAGE_UI_CURRENCY_VALENBRAINZ_STACK_0");
        boolean[] brainsEaten = izMode.getBrainsEaten();
        float brainX = colToWorldX(0);
        float brainSize = 65f;
        for (int lane = 0; lane < lanes; lane++) {
            if (lane < brainsEaten.length && !brainsEaten[lane]) {
                float brainY = laneToWorldY(lane);
                if (brainRegion != null) {
                    batch.draw(brainRegion, brainX - brainSize / 2f, brainY - brainSize / 2f, brainSize, brainSize);
                } else {
                    batch.end();
                    shapeRenderer.setProjectionMatrix(camera.combined);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(Color.MAGENTA);
                    shapeRenderer.circle(brainX, brainY, 18f);
                    shapeRenderer.end();
                    batch.setProjectionMatrix(camera.combined);
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

    /**
     * Draws one animation frame, honouring the optional {@code partsVisibility}
     * map (e.g. a zombie's armour damage layers). The normal {@code PamPlayer}
     * draw has no visibility overload, so this falls back to the reflective
     * visibility-aware draw (which keeps the requested scale).
     */
    private void drawFrame(FrameConfig frameConfig) {
        if (frameConfig.partsVisibility == null) {
            PvZ2.pamPlayer.draw(batch, frameConfig.pamPath, frameConfig.label,
                    frameConfig.stateTime, frameConfig.position.x, frameConfig.position.y,
                    frameConfig.scale.x, frameConfig.scale.y, frameConfig.looping);
        } else {
            PamActor.drawWithVisibility(batch, frameConfig.pamPath, frameConfig.label,
                    frameConfig.stateTime, frameConfig.position.x, frameConfig.position.y,
                    frameConfig.scale.x, frameConfig.looping, frameConfig.partsVisibility);
        }
    }
}
