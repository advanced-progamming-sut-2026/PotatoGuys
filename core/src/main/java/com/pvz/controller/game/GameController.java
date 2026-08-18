package com.pvz.controller.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.Hitbox;
import com.pvz.models.entities.LawnMower;
import com.pvz.models.entities.effects.LootDrop;
import com.pvz.models.entities.effects.Effect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelLoader;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.games.modes.capabilities.ZombiePlacer;
import com.pvz.models.games.modes.variants.IZombieMode;
import com.pvz.models.games.modes.variants.VaseBreakerMode;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.user.MyPlant;
import com.pvz.view.GameModesMenu;
import com.pvz.view.TravelLogMenu;
import com.pvz.view.game.GameScreen;
import com.pvz.view.game.GameUiModal;
import com.pvz.view.game.GameOverPopup;
import com.pvz.view.game.GameWinPopup;
import com.pvz.view.game.PauseMenuPopup;
import com.pvz.view.game.PlantSelectModal;
import com.pvz.view.PamActor;
import com.pvz.view.PlantData;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private final String seasonName;
    private final int levelNumber;

    private SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport viewport;
    private Stage stage;
    private final OrthographicCamera camera;

    private float stateTime;
    private GameContext ctx;
    private Level level;
    private PlantSelectModal plantSelectModal;
    private GameUiModal gameUiModal;
    private State currentState = State.PANNING_FORWARD;

    private TextureRegion[] backgroundTextures;
    private boolean isIZombie = false;

    private final Vector3 touchPos = new Vector3();

    private float panBackTime = 0f;
    private final float transitionDuration = 3.0f;
    private float startX;
    private float endX;
    private boolean gameStarted = false;
    private final float panBackDuration = 2.5f;
    private Label readyPlantLabel;
    private float readyPlantTimer = 0f;
    private final float readyPlantDuration = 2.0f;

    private boolean paused = false;
    private Table pauseOverlay;
    private boolean cardsInitialized = false;

    /** Debug: draws every entity's hitbox rectangle (toggle with F1). */
    private boolean showHitboxes = true;

    /** Placement-preview ghost: plays the selected plant's idle PAM under the cursor. */
    private float previewStateTime;
    private PlantCard previewPlantCard;
    private ZombieCard previewZombieCard;

    public GameController(String seasonName, int levelNumber){
        this.seasonName=seasonName;
        this.levelNumber=levelNumber;

        this.batch= PvZ2.batch;
        shapeRenderer = new ShapeRenderer();
        stateTime=0;

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        stage = new Stage(new FitViewport(1280, 720));

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (currentState == State.PLAYING && !paused) {
                    if (ctx != null) {
                        // تبدیل ورودی ماوس/لمس به مختصات دقیق World
                        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(touchPos);

                        // If a zombie card is selected, prioritize zombie placement
                        boolean zombieCardSelected = gameUiModal != null && gameUiModal.getSelectedZombieCard() != null;

                        if (!zombieCardSelected) {
                            // ۱. ابتدا کلیک روی خورشید بررسی می‌شود
                            if (checkSunClick(touchPos.x, touchPos.y)) {
                                if (gameUiModal != null) {
                                    gameUiModal.setSelectedCard(null);
                                }
                                return true;
                            }

                            // ۱.۱. کلیک روی سکه‌ی افتاده از زامبی بررسی می‌شود
                            if (checkLootClick(touchPos.x, touchPos.y)) {
                                if (gameUiModal != null) {
                                    gameUiModal.setSelectedCard(null);
                                }
                                return true;
                            }

                            // ۱.۱. اگر کارتی انتخاب نشده باشد، کلیک روی گیاهِ کاشته‌شده
                            //      پلنت‌فود آن را اجرا می‌کند
                            if (gameUiModal == null || gameUiModal.getSelectedCard() == null) {
                                if (checkPlantFoodClick(touchPos.x, touchPos.y)) {
                                    return true;
                                }
                            }

                            // Vase breaking: click on a vase tile to break it (no card selected)
                            if (gameUiModal == null || gameUiModal.getSelectedCard() == null) {
                                if (ctx.getMode() instanceof VaseBreakerMode vbMode) {
                                    Tile clickedTile = ctx.getMap().getTileAt(touchPos.x, touchPos.y);
                                    if (clickedTile != null) {
                                        int col = clickedTile.getCol();
                                        int lane = clickedTile.getLane();
                                        com.pvz.models.games.map.behaviors.VaseBehavior vase = null;
                                        for (var b : clickedTile.getBehaviors()) {
                                            if (b instanceof com.pvz.models.games.map.behaviors.VaseBehavior vb) {
                                                vase = vb;
                                                break;
                                            }
                                        }
                                        if (vase != null && !vase.isBroken()) {
                                            vbMode.breakVase(ctx, col, lane);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }

                        // ۲. کاشت گیاه در صورت انتخاب کارت
                        if (gameUiModal != null && gameUiModal.getSelectedCard() != null) {
                            Tile hoveredTile = ctx.getMap().getTileAt(touchPos.x, touchPos.y);
                            if (hoveredTile != null) {
                                PlantCard card = gameUiModal.getSelectedCard();
                                if (ctx.getMode() instanceof PlantPlacer placer) {
                                    int col = hoveredTile.getCol();
                                    int lane = hoveredTile.getLane();

                                    if (placer.isValidPlacement(ctx, col, lane, card)) {
                                        placer.handlePlacement(ctx, col, lane, card);
                                        gameUiModal.setSelectedCard(null);
                                        return true;
                                    }
                                }
                            }
                        }

                        // ۲.۱. کاشت زامبی در صورت انتخاب کارت زامبی (IZombie)
                        if (gameUiModal != null && gameUiModal.getSelectedZombieCard() != null) {
                            Tile hoveredTile = ctx.getMap().getTileAt(touchPos.x, touchPos.y);
                            if (hoveredTile != null) {
                                ZombieCard zCard = gameUiModal.getSelectedZombieCard();
                                if (ctx.getMode() instanceof ZombiePlacer placer) {
                                    int col = hoveredTile.getCol();
                                    int lane = hoveredTile.getLane();

                                    if (placer.isValidPlacement(ctx, col, lane, zCard)) {
                                        placer.handlePlacement(ctx, col, lane, zCard);
                                        gameUiModal.setSelectedZombieCard(null);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });

        ctx = AppContext.getInstance().getGameContext();

        level = LevelLoader.loadLevel(seasonName, levelNumber);

        plantSelectModal = new PlantSelectModal(level, this::startGameSession);
        stage.addActor(plantSelectModal);

        gameUiModal = new GameUiModal(this::pauseGame);
        stage.addActor(gameUiModal);

        backgroundTextures=new TextureRegion[3];
        switch (seasonName.toLowerCase()){
            case "ancient egypt"->{
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_RIGHT");
            }
            case "frostbite caves"->{
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_ICEAGE_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_ICEAGE_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_ICEAGE_TEXTURE_RIGHT");
            }
            case "dark ages"->{
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_DARK_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_DARK_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_DARK_TEXTURE_RIGHT");
            }
            case "big wave beach"->{
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_BEACH_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_BEACH_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_BEACH_TEXTURE_RIGHT");
            }
            case "izombie"->{
                isIZombie = true;
                com.badlogic.gdx.graphics.Texture left = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("textures/backgrounds/IZOMBIE/texture_left.png"));
                com.badlogic.gdx.graphics.Texture mid = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("textures/backgrounds/IZOMBIE/texture.png"));
                com.badlogic.gdx.graphics.Texture right = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("textures/backgrounds/IZOMBIE/texture_right.png"));
                backgroundTextures[0] = new TextureRegion(left);
                backgroundTextures[1] = new TextureRegion(mid);
                backgroundTextures[2] = new TextureRegion(right);
            }
            default->{
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_RIGHT");
            }
        }

        float wCenter = backgroundTextures[1].getRegionWidth();
        float wRight = backgroundTextures[2].getRegionWidth();

        startX = wCenter / 2f;
        endX = wCenter + wRight - 640f;

        camera.position.set(startX, 720f / 2f, 0);
        camera.update();

        readyPlantLabel = new Label("Ready... Plant!", PvzSkin.get(), "big");
        readyPlantLabel.setColor(Color.RED);
        readyPlantLabel.setFontScale(1.5f);
        readyPlantLabel.setAlignment(Align.center);
        readyPlantLabel.setVisible(false);

        Table labelTable = new Table();
        labelTable.setFillParent(true);
        labelTable.center();
        labelTable.add(readyPlantLabel);
        stage.addActor(labelTable);
    }

    public void update(float dt){
        camera.update();
        previewStateTime += dt;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showHitboxes = !showHitboxes;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (paused) {
                resumeGame();
            } else if (currentState == State.PLAYING) {
                pauseGame();
            }
        }

        switch (currentState) {
            case PANNING_FORWARD:
                stateTime += dt;
                float progressFwd = Math.min(1f, stateTime / transitionDuration);
                float smoothFwd = com.badlogic.gdx.math.Interpolation.fade.apply(progressFwd);
                float currentXFwd = com.badlogic.gdx.math.MathUtils.lerp(startX, endX, smoothFwd);
                camera.position.set(currentXFwd, 720f / 2f, 0);

                if (progressFwd >= 1f) {
                    if (level != null && !level.hasPreGame()) {
                        startGameSession();
                    } else {
                        currentState = State.PLANT_SELECT;
                        plantSelectModal.setVisible(true);
                        Gdx.input.setInputProcessor(stage);
                    }
                }
                break;

            case PLANT_SELECT:
                camera.position.set(endX, 720f / 2f, 0);
                break;

            case PANNING_BACK:
                panBackTime += dt;
                float progressBack = Math.min(1f, panBackTime / panBackDuration);
                float smoothBack = com.badlogic.gdx.math.Interpolation.fade.apply(progressBack);
                float currentXBack = com.badlogic.gdx.math.MathUtils.lerp(endX, startX, smoothBack);
                camera.position.set(currentXBack, 720f / 2f, 0);

                if (progressBack >= 1f) {
                    currentState = State.READY_PLANT;
                    readyPlantLabel.setVisible(true);
                    readyPlantTimer = 0f;
                }
                break;

            case READY_PLANT:
                camera.position.set(startX, 720f / 2f, 0);
                readyPlantTimer += dt;
                if (readyPlantTimer >= readyPlantDuration) {
                    readyPlantLabel.setVisible(false);
                    currentState = State.PLAYING;
                    gameStarted = true;
                }
                break;

            case PLAYING:
                camera.position.set(startX, 720f / 2f, 0);
                gameUiModal.updateHud();
                if (ctx.isGameOver() && !paused) {
                    showGameEndPopup();
                }
                break;
        }

        // Check sun & coin-drop clicks in PLAYING state on left click (only when no card selected)
        if (currentState == State.PLAYING && !paused && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            boolean zombieCardActive = gameUiModal != null && gameUiModal.getSelectedZombieCard() != null;
            if (!zombieCardActive) {
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(touchPos);
                if (checkSunClick(touchPos.x, touchPos.y) || checkLootClick(touchPos.x, touchPos.y)) {
                    if (gameUiModal != null) {
                        gameUiModal.setSelectedCard(null);
                    }
                }
            }
        }

        // ۴. آپدیت Engine
        if (currentState == State.PLAYING && !paused) {
            GameEngine.getInstance().update(dt);
            if (!cardsInitialized) {
                cardsInitialized = true;
                gameUiModal.initCards();
                gameUiModal.setVisible(true);
            }
        }
    }

    private void pauseGame() {
        if (currentState != State.PLAYING || paused) {
            return;
        }

        paused = true;

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setTouchable(Touchable.enabled);
        overlay.setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.62f)));

        overlay.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        PauseMenuPopup popup = new PauseMenuPopup(this::saveAndExit, this::restartLevel, this::resumeGame);
        overlay.add(popup).center();

        pauseOverlay = overlay;
        stage.addActor(pauseOverlay);
        pauseOverlay.toFront();
    }

    private void resumeGame() {
        if (!paused) {
            return;
        }
        paused = false;
        if (pauseOverlay != null) {
            pauseOverlay.remove();
            pauseOverlay = null;
        }
    }

    private void restartLevel() {
        resumeGame();
        Gdx.app.postRunnable(() -> PvZ2.instance.setScreen(new GameScreen(seasonName, levelNumber)));
    }

    private void showGameEndPopup() {
        paused = true;

        boolean won = false;
        if (ctx.getMode() instanceof IZombieMode izMode) {
            boolean[] brains = izMode.getBrainsEaten();
            won = true;
            for (boolean b : brains) {
                if (!b) { won = false; break; }
            }
        } else {
            won = ctx.getZombies().isEmpty();
        }

        Runnable exitAction = () -> Gdx.app.postRunnable(() -> {
            if (ctx.getMode() instanceof IZombieMode) {
                PvZ2.instance.setScreen(new TravelLogMenu(PvZ2.instance));
            } else {
                PvZ2.instance.setScreen(new GameModesMenu(PvZ2.instance));
            }
        });

        Table popup;
        if (won) {
            String title = "Level Complete!";
            String msg = "You defeated the zombies!";
            if (ctx.getMode() instanceof IZombieMode) {
                title = "I, ZOMBIE COMPLETE!";
                msg = "All five brains were eaten!";
            }
            Runnable nextAction = () -> Gdx.app.postRunnable(() ->
                    PvZ2.instance.setScreen(new GameScreen(seasonName, levelNumber + 1)));
            popup = new GameWinPopup(title, msg, "EXIT TO MAP", exitAction, "NEXT LEVEL", nextAction);
        } else {
            String title = "THE ZOMBIES\nATE YOUR\nBRAINS!";
            Runnable retryAction = () -> Gdx.app.postRunnable(() ->
                    PvZ2.instance.setScreen(new GameScreen(seasonName, levelNumber)));
            popup = new GameOverPopup(title, "EXIT TO MAP", exitAction, "RETRY", retryAction);
        }

        stage.addActor(popup);
        popup.toFront();
    }

    private void saveAndExit() {
        resumeGame();
        Gdx.app.postRunnable(() -> {
            if (ctx != null && ctx.getMode() instanceof IZombieMode) {
                PvZ2.instance.setScreen(new TravelLogMenu(PvZ2.instance));
            } else {
                PvZ2.instance.setScreen(new GameModesMenu(PvZ2.instance));
            }
        });
    }

    public void draw(){
        batch.setProjectionMatrix(camera.combined);
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

        if (currentState == State.PLAYING && gameUiModal != null && ctx != null) {
            boolean hasSelectedCard = gameUiModal.getSelectedCard() != null || gameUiModal.getSelectedZombieCard() != null;
            if (hasSelectedCard) {
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(touchPos);

                Tile hoveredTile = ctx.getMap().getTileAt(touchPos.x, touchPos.y);

                if (hoveredTile != null) {
                    drawPlacementHighlights(hoveredTile);
                }
            }
        }
        drawDebugShapes();
    }

    public void resize(int width , int hight){
        viewport.update(width , hight);
        stage.getViewport().update(width, hight);
    }

    private void drawBackground(){
        float x = -backgroundTextures[0].getRegionWidth();
        batch.draw(backgroundTextures[0], x, 0);
        batch.draw(backgroundTextures[1], 0, 0);
        batch.draw(backgroundTextures[2], backgroundTextures[1].getRegionWidth(), 0);
    }

    private void drawInactiveLawnMowers(int row) {
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
            if (p.getLane() == row) {
                FrameConfig fc = p.draw();
                if (fc != null) {
                    PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                        fc.stateTime, fc.position.x, fc.position.y,
                        fc.scale.x, fc.scale.y, fc.looping);
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

    private void drawZombies(int row) {
        for (Zombie z : ctx.getZombies()) {
            if (GameController.worldYtoLane(z.getY()) == row) {
                FrameConfig fc = z.draw();
                if (fc != null) {
                    batch.setColor(fc.r, fc.g, fc.b, fc.a);
                    drawFrame(fc);
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
        LawnMower lm = ctx.getLawnMowers()[row];
        if (lm != null && lm.isTriggered()) {
            FrameConfig fc = lm.draw();
            PvZ2.pamPlayer.draw(batch, fc.pamPath, fc.label,
                fc.stateTime, fc.position.x, fc.position.y,
                fc.scale.x, fc.scale.y, fc.looping);
        }
    }



    /**
     * Draws one animation frame, honouring the optional {@code partsVisibility}
     * map (e.g. a zombie's armour damage layers). The normal {@code PamPlayer}
     * draw has no visibility overload, so this falls back to the reflective
     * visibility-aware draw (which keeps the requested scale).
     */
    private void drawFrame(FrameConfig frameConfig){
        if (frameConfig.partsVisibility == null){
            PvZ2.pamPlayer.draw(batch,frameConfig.pamPath,frameConfig.label,
                frameConfig.stateTime,frameConfig.position.x, frameConfig.position.y,
                frameConfig.scale.x, frameConfig.scale.y, frameConfig.looping);
        } else {
            PamActor.drawWithVisibility(batch,frameConfig.pamPath,frameConfig.label,
                frameConfig.stateTime,frameConfig.position.x, frameConfig.position.y,
                frameConfig.scale.x,frameConfig.looping,frameConfig.partsVisibility);
        }
    }



    /**
     * Draws the "ghost" of the currently selected seed packet following the mouse:
     * while a plant card is armed, the plant's idle PAM animation is played under the
     * cursor, semi-transparent, until the plant is placed on a tile (or deselected).
     */
    private void drawPlacementPreview() {
        if (currentState != State.PLAYING || paused) return;

        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPos);

        // Plant preview
        if (gameUiModal != null && gameUiModal.getSelectedCard() != null) {
            PlantCard card = gameUiModal.getSelectedCard();
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
                            touchPos.x, touchPos.y, 0.7f, 0.7f, true);
                    batch.setColor(Color.WHITE);
                }
            }
            return;
        }

        // Zombie preview
        if (gameUiModal != null && gameUiModal.getSelectedZombieCard() != null) {
            ZombieCard zCard = gameUiModal.getSelectedZombieCard();
            if (zCard != previewZombieCard) {
                previewZombieCard = zCard;
                previewStateTime = 0f;
            }
            com.pvz.models.entities.zombies.data.ZombiePropertySheet sheet =
                com.pvz.models.entities.zombies.data.ZombieRegistry.getInstance().getSheet(
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
                                touchPos.x, touchPos.y, scale, true, partsVisibility);
                    } else {
                        PvZ2.pamPlayer.draw(batch, pamPath, idleLabel, previewStateTime,
                                touchPos.x, touchPos.y, scale, scale, true);
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
                ? armorAlias.substring(armorAlias.indexOf(':') + 1) : armorAlias;
            com.pvz.models.entities.zombies.data.ArmorPropertySheet aSheet =
                com.pvz.models.entities.zombies.data.ZombieRegistry.getInstance().getArmorSheet(cleanAlias);
            if (aSheet == null) continue;
            com.pvz.models.entities.zombies.armor.ArmorType type =
                com.pvz.models.entities.zombies.armor.ArmorType.fromString(aSheet.getArmorType());
            String[] layers = type.pamLayers();
            if (layers == null) continue;
            if (visibility == null) visibility = new java.util.HashMap<>();
            for (int i = 0; i < layers.length; i++) {
                visibility.put(layers[i], i == 0);
            }
            String container = type.pamContainerName();
            if (container != null) visibility.put(container, true);
            for (String alive : type.pamAliveParts()) visibility.put(alive, true);
            for (String crit : type.pamCriticalParts()) visibility.put(crit, false);
        }
        return visibility;
    }


    /**
     * Highlights the whole row (lane) and whole column under the hovered tile in white,
     * matching the reference implementation's placement preview: while a plant card is
     * armed, hovering a tile paints its entire lane and column so the player sees exactly
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
        if (!(ctx.getMode() instanceof IZombieMode izMode)) return;

        int lanes = ctx.getMap().getLanes();

        // Red line at the placement boundary (shapeRenderer)
        batch.end();
        float redLineX = colToWorldX(izMode.getRedLineColumn()) - GameMap.TILE_WIDTH / 2f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(redLineX, GameMap.TOP_LANE_Y - (lanes - 1) * GameMap.TILE_HEIGHT, 4f, lanes * GameMap.TILE_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Brain indicators at column 0 for each lane
        com.badlogic.gdx.graphics.g2d.TextureRegion brainRegion = PvZ2.textureBank.region("IMAGE_UI_CURRENCY_VALENBRAINZ_STACK_0");
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
            if (z.isDead()) continue;
            PvZ2.pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", "animation",
                    stateTime, z.getX(), z.getY() + 55f + bob, sunScale, sunScale, true);
        }
    }

    private void drawDebugShapes(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        // ۶. رسم خورشیدها و خطوط دیباگ گرید
        if (ctx != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // رسم خورشیدها
            shapeRenderer.setColor(Color.YELLOW);
            /*for (Sun sun : new ArrayList<>(context.getSuns())) {
                if (!sun.isDone()) {
                    shapeRenderer.circle(sun.getX(), sun.getY(), 50);
                }
            }*/
            // رسم خطوط گرید دیباگ
            shapeRenderer.setColor(Color.RED);
            // for (int i = 0; i < ctx.getMap().getLanes(); i++) {
            //     float gridY = GameMap.TOP_LANE_Y - i * GameMap.TILE_HEIGHT;
            //     shapeRenderer.rect(0, gridY - 0.5f, GameScreen.SCREEN_WIDTH, 1);
            // }
            // for (int i = 0; i < ctx.getMap().getColumns(); i++) {
            //     float gridX = GameMap.START_X + i * GameMap.TILE_WIDTH;
            //     shapeRenderer.rect(gridX - 0.5f, 0, 1, GameScreen.SCREEN_HEIGHT);
            // }

            // for(Plant a : ctx.getPlants()){
            //     shapeRenderer.circle(GameController.xToWorldX(a.getCol()), GameController.yToWorldY(a.getLane()), 10);
            // }

            // for(Projectile a : ctx.getProjectiles()){
            //     shapeRenderer.circle(a.getX(), a.getY(), 10);
            // }

            // for(Zombie a : ctx.getZombies()){
            //     shapeRenderer.circle(a.getX(),a.getY(), 10);
            // }

            shapeRenderer.end();

            // Hitbox debug (outline)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            drawHitboxes();
            shapeRenderer.end();
        }
    }

    // ── Hitbox debug ─────────────────────────────────────────────────────────

    private void drawHitboxes() {
        if (!showHitboxes) return;

        drawEntityHitboxes(Color.GREEN, ctx.getPlants());
        drawEntityHitboxes(Color.RED, ctx.getZombies());
        drawEntityHitboxes(Color.CYAN, ctx.getProjectiles());
        drawEntityHitboxes(Color.YELLOW, ctx.getSuns());
        for (LawnMower m : ctx.getLawnMowers()) {
            if (m != null) drawEntityHitbox(Color.ORANGE, m);
        }
    }

    private void drawEntityHitboxes(Color color, List<? extends Entity> entities) {
        for (Entity e : entities) {
            drawEntityHitbox(color, e);
        }
    }

    private void drawEntityHitbox(Color color, Entity e) {
        Hitbox hitbox = e.getHitbox();
        if (hitbox == null) return;
        Rectangle r = hitbox.getRectangle();
        shapeRenderer.setColor(color);
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
    }

    public void startGameSession() {
        try {
            if (level != null) {
                GameEngine.getInstance().reset();
                GameContext newContext = new GameContext(level);
                for (PlantType pt : plantSelectModal.getSelectedPlants()) {
                    MyPlant owned = null;
                    try {
                        owned = AppContext.getInstance().getCurrentUser().getProfile().getCollection().getPlant(pt);
                    } catch (Exception ignored) {}
                    PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(pt);
                    int sunCost = (sheet != null) ? sheet.getSunCost() : 50;
                    float recharge = (sheet != null) ? sheet.getRechargeSeconds() : 5f;

                    MyPlant myPlant = owned;
                    if (myPlant == null) {
                        myPlant = new MyPlant();
                        myPlant.setType(pt);
                        myPlant.setLevel(1);
                    }
                    newContext.addCard(new PlantCard(myPlant, sunCost, recharge));
                }
                AppContext.getInstance().setGameContext(newContext);
                ctx = newContext;

                plantSelectModal.setVisible(false);
                currentState = State.PANNING_BACK;
                panBackTime = 0f;
                Gdx.input.setInputProcessor(stage);

                Gdx.app.log("GameScreen", "Starting camera pan back for " + seasonName + " Level " + levelNumber);
            } else {
                Gdx.app.error("GameScreen", "Failed to load level: " + seasonName + " Level " + levelNumber);
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error starting game session", e);
        }
    }

    private boolean checkSunClick(float worldX, float worldY) {
        if (ctx != null) {
            for (Sun sun : new ArrayList<>(ctx.getSuns())) {
                if (sun.isDone()) continue;
                float sunX = sun.getX();
                float sunY = sun.getY();
                float dist = (float) Math.hypot(worldX - sunX, worldY - sunY);
                if (dist < 50f) {
                    sun.collect(ctx);
                    Gdx.app.log("GameScreen", "Sun collected! Amount: " + sun.getAmount());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * If the click lands on a fully-landed zombie loot drop, coins and diamonds
     * fly to their on-screen wallet (banked on arrival); the pot unlocks a random
     * locked greenhouse pot immediately.
     */
    private boolean checkLootClick(float worldX, float worldY) {
        if (ctx == null) {
            return false;
        }
        for (LootDrop drop : new ArrayList<>(ctx.getLootDrops())) {
            if (drop.isDone() || !drop.isCollectable()) continue;
            float dist = (float) Math.hypot(worldX - drop.getX(), worldY - drop.getY());
            if (dist < 55f) {
                if (drop.getType() == LootDrop.LootType.POT) {
                    drop.collect();
                    Gdx.app.log("GameScreen", "Pot drop clicked, a greenhouse pot was unlocked.");
                } else {
                    Vector2 target = lootWalletWorld(drop.getType());
                    drop.flyTo(target.x, target.y);
                    Gdx.app.log("GameScreen", "Loot drop clicked, flying to wallet: +" + drop.getAmount() + " "
                        + drop.getType().name().toLowerCase() + "s.");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * The on-screen wallet for a loot type, in world coordinates. The game viewport
     * and the HUD stage are both FitViewport(1280, 720) with a bottom-left origin,
     * so the wallet's stage position is already a valid world position.
     */
    private Vector2 lootWalletWorld(LootDrop.LootType type) {
        if (gameUiModal == null) {
            return type == LootDrop.LootType.DIAMOND
                ? new Vector2(980f, 680f)
                : new Vector2(1150f, 680f);
        }
        Vector2 stagePos = type == LootDrop.LootType.DIAMOND
            ? gameUiModal.getGemWalletStagePosition()
            : gameUiModal.getCoinWalletStagePosition();
        return new Vector2(stagePos.x, stagePos.y);
    }

    /**
     * If the click lands inside a planted plant's hitbox, triggers the plant's
     * Plant Food effect. Works unconditionally (no plant food is consumed).
     *
     * @return {@code true} if the click was consumed by a plant food
     */
    private boolean checkPlantFoodClick(float worldX, float worldY) {
        if (ctx == null) {
            return false;
        }
        for (Plant plant : new ArrayList<>(ctx.getPlants())) {
            if (plant.isDead()) {
                continue;
            }
            if (plant.getHitbox() != null
                    && plant.getHitbox().getRectangle().contains(worldX, worldY)) {
                plant.triggerPlantFood(ctx);
                ctx.log("[PlantFood] " + plant.getSheet().getName()
                        + " at (" + plant.getCol() + "," + plant.getLane() + ") used Plant Food!");
                return true;
            }
        }
        return false;
    }

    public GameContext getCtx(){
        return ctx;
    }

    public PlantSelectModal getPlantSelectModal(){
        return plantSelectModal;
    }

    public GameUiModal getGameUiModal(){
        return gameUiModal;
    }

    public State getCurrentState(){
        return currentState;
    }

    public OrthographicCamera getCamera(){
        return camera;
    }

    public Stage getStage(){
        return stage;
    }

    public ShapeRenderer getShapeRenderer(){
        return shapeRenderer;
    }

    // This method returns the world coordinates of the middle of column
    public static float colToWorldX(int col){
        return GameMap.START_X + (col * GameMap.TILE_WIDTH) + (GameMap.TILE_WIDTH / 2f);
    }

    public static float laneToWorldY(int lane){
        return GameMap.TOP_LANE_Y - (lane * GameMap.TILE_HEIGHT) + (GameMap.TILE_HEIGHT / 2f);
    }

    public static int worldXtoCol(float x) {
        return (int) Math.floor((x - GameMap.START_X) / GameMap.TILE_WIDTH);
    }

    public static int worldYtoLane(float y) {
        return (int) Math.ceil((GameMap.TOP_LANE_Y - y) / GameMap.TILE_HEIGHT);
    }

    public static float xToWorldX(float x){
        return GameMap.START_X + (x * GameMap.TILE_WIDTH);
    }
    public static float yToWorldY(float y){
        return GameMap.TOP_LANE_Y - (y * GameMap.TILE_HEIGHT);
    }
/*

    private final GameContext context;

    public GameController(GameContext context) {
        this.context = context;
    }

    public Result advanceTime(Matcher matcher) {
        int ticks = Integer.parseInt(matcher.group("ticks"));
        context.getEngine().advanceTime(ticks);
        context.addCurrentTick(ticks);

        String map = context.getMode().renderMap(context);
        if (context.isGameOver()) {
            handleGameOverState();
            return new Result("Game Over", new pvz.view.OldMainMenu());
        }
        return new Result("Time advanced by " + ticks + " ticks.\n" + map);
    }

    private void handleGameOverState() {
        User user = AppContext.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        boolean won = context.getZombies().isEmpty();
        evaluateQuestsAndScore(user, won);
        user.saveUser();
    }

    private void evaluateQuestsAndScore(User user, boolean won) {
        QuestEvaluator.evaluateAll(
                user.getQuestLog(),
                context.getGameStats(),
                won,
                context.getCurrentSun(),
                user.getSetting().getDifficulty(),
                user,
                context);

        int gameScore = context.getGameStats().calculateScore(won);
        if (gameScore > user.getScore().getHighestScore()) {
            user.getScore().setHighestScore(gameScore);
        }

        handleLevelOrMiniGameWin(user, won);
    }

    private void handleLevelOrMiniGameWin(User user, boolean won) {
        if (!won) return;

        boolean isMiniGame = (context.getMode() instanceof pvz.models.games.modes.variants.VaseBreakerMode
                || context.getMode() instanceof pvz.models.games.modes.variants.IZombieMode
                || context.getMode() instanceof pvz.models.games.modes.variants.BeghouledMode);

        if (isMiniGame) {
            user.getScore().setMiniGamesPassed(user.getScore().getMiniGamesPassed() + 1);
            user.getProfile().getNews().getMessages().add(
                    new pvz.models.user.Message("New Mini-Game Completed: " + context.getSeasonName()
                            + " Level " + context.getLevelNumber() + "!"));
        } else {
            user.getScore().setLastLevel(context.getLevelNumber());
            user.getScore().setLastSeason(resolveChapterNumber(context.getSeasonName()));

            pvz.models.games.seasons.SeasonManager manager = new pvz.models.games.seasons.SeasonManager();
            manager.unlockNextLevel(user, context.getSeasonName(), context.getLevelNumber());

            int nextLevelNumber = context.getLevelNumber() + 1;
            user.getProfile().getNews().getMessages().add(
                    new pvz.models.user.Message("New Level Unlocked: Level " + nextLevelNumber + " in "
                            + context.getSeasonName() + "!"));
        }
    }

    private int resolveChapterNumber(String season) {
        if (season == null) {
            return 1;
        }
        if (season.equalsIgnoreCase("Ancient Egypt")) {
            return 1;
        }
        if (season.equalsIgnoreCase("Frostbite Caves")) {
            return 2;
        }
        if (season.equalsIgnoreCase("Dark Ages")) {
            return 3;
        }
        if (season.equalsIgnoreCase("Big Wave Beach")) {
            return 4;
        }
        return 1;
    }

    public Result plantPlant(Matcher matcher) {
        String plantType = matcher.group("plantType");
        int col = Integer.parseInt(matcher.group("plantX"));
        int lane = Integer.parseInt(matcher.group("plantY"));

        GameMode mode = context.getMode();
        if (mode instanceof PlantPlacer placer) {
            PlantCard card = placer.findCard(context, plantType);
            if (card == null) {
                return new Result("No such plant card '" + plantType + "'.");
            }
            if (!placer.isValidPlacement(context, col, lane, card)) {
                return new Result("Cannot place " + plantType + " at (" + col + ", " + lane + ").");
            }
            placer.handlePlacement(context, col, lane, card);
        } else {
            return new Result("You cannot plant in this game mode!");
        }
        return new Result(plantType + " placed at (" + col + ", " + lane + ").");
    }

    public Result plantZombie(Matcher matcher) {
        String zombieType = matcher.group("zombieType");
        int col = Integer.parseInt(matcher.group("zombieX"));
        int lane = Integer.parseInt(matcher.group("zombieY"));

        GameMode mode = context.getMode();
        if (mode instanceof ZombiePlacer placer) {
            ZombieCard card = placer.findCard(context, zombieType);
            if (card == null) {
                return new Result("No such zombie card '" + zombieType + "'.");
            }
            if (!placer.isValidPlacement(context, col, lane, card)) {
                return new Result("Cannot place " + zombieType + " at (" + col + ", " + lane + ").");
            }
            placer.handlePlacement(context, col, lane, card);
        } else {
            return new Result("You cannot place zombies in this game mode!");
        }
        return new Result(zombieType + " placed at (" + col + ", " + lane + ").");
    }

    public Result breakVase(Matcher matcher) {
        GameMode mode = context.getMode();
        int col = Integer.parseInt(matcher.group("vaseX"));
        int lane = Integer.parseInt(matcher.group("vaseY"));

        if (mode instanceof VaseBreakerMode vasemode) {
            vasemode.breakVase(context, col, lane);
            return new Result("");
        }

        return new Result("You cannot break vase in this game mode!");
    }

    public Result pluckPlant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("pluckX"));
        int y = Integer.parseInt(matcher.group("pluckY"));

        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
            return new Result("Invalid coordinates.");
        }

        Tile tile = context.getMap().getTile(x, y);
        if (tile.getPlants().isEmpty()) {
            return new Result("No plant to pluck at (" + x + ", " + y + ").");
        }

        for (int i = 0; i < tile.getPlants().size(); i++) {
            context.removePlant(tile.getPlants().get(i));
            i--;
        }
        tile.getPlants().clear();

        return new Result("Plant plucked from (" + x + ", " + y + ").");
    }

    public Result collectSun(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("sunX"));
        int y = Integer.parseInt(matcher.group("sunY"));

        for (Sun sun : new ArrayList<>(context.getSuns())) {
            if (sun.getCol() == x && sun.getLane() == y && !sun.isDone()) {
                sun.collect(context);
                return new Result("Sun collected!");
            }
        }
        return new Result("No sun found at these coordinates.");
    }

    public Result feedPlant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("feedX"));
        int y = Integer.parseInt(matcher.group("feedY"));

        if (context.getPlantFoodCount() <= 0) {
            return new Result("No plant food available.");
        }
        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
            return new Result("Invalid coordinates.");
        }

        List<Plant> plantsAt = context.getPlantsAt(x, y);
        if (plantsAt.isEmpty()) {
            return new Result("No plant at (" + x + ", " + y + ").");
        }

        Plant target = plantsAt.getLast();
        if (!context.spendPlantFood()) {
            return new Result("Failed to consume plant food.");
        }

        target.triggerPlantFood(context);
        return new Result("Plant food used on " + target.getSheet().getName() + " at (" + x + ", " + y + ")!");
    }

    public Result startZombieWavesCommand(Matcher matcher) {
        GameContext context = AppContext.getInstance().getGameContext();

        if (context.getMode() instanceof StartWaves mode) {
            if (!mode.isPreparationPhase()) {
                return new Result("Zombie waves have already started!");
            }
            mode.startZombieWaves(context);
            return new Result("Zombie waves started successfully!");
        } else {
            return new Result("This command is only available in modes that support starting zombie waves.");
        }
    }

    public Result releaseNuke(Matcher matcher) {
        int killed = 0;
        List<TickAware> entities = context.getEngine().getEntities();
        for (TickAware entity : entities) {
            if (entity instanceof Zombie) {
                ((Zombie) entity).takeDamage(999999f, true);
                killed++;
            }
        }
        return new Result("Nuke released! Killed " + killed + " zombies.");
    }

    public Result cheatCooldown(Matcher matcher) {
        List<Card> cards = context.getCards();
        int reset = 0;
        for (Card card : cards) {
            if (card.getCooldown() > 0) {
                card.setCooldown(0);
                reset++;
            }
        }
        return new Result("Reset cooldown for " + reset + " card(s).");
    }

    public Result cheatPlantFood(Matcher matcher) {
        if (context.getPlantFoodCount() > 3) {
            return new Result("Plant food slots are full.");
        }
        context.addPlantFood(1);
        return new Result("Added 1 plant food.");
    }

    public Result cheatSun(Matcher matcher) {
        int amount = Integer.parseInt(matcher.group("sunCount"));
        context.addSun(amount);
        return new Result("Added " + amount + " sun.");
    }

    public Result cheatSpawnZombie(Matcher matcher) {
        String zombieType = matcher.group("zombieType");
        int col = Integer.parseInt(matcher.group("zombieX"));
        int lane = Integer.parseInt(matcher.group("zombieY"));

        ZombieType type = ZombieType.fromTypeString(zombieType);
        if (type == null) {
            return new Result("No such zombie type '" + zombieType + "'.");
        }

        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
            return new Result("The position is uncorrent!");
        }

        Zombie zombie = new ZombieFactory().create(type.getAlias(), col, lane, context, 0, 2);
        context.spawnZombie(zombie);
        return new Result("Zombie spawned!");
    }

    public Result showMap(Matcher matcher) {
        return new Result(context.getMode().renderMap(context));
    }

    public Result showCards(Matcher matcher) {
        return new Result(context.getMode().getCardsStatus(context));
    }

    public Result showSun(Matcher matcher) {
        return new Result("Current Sun: " + context.getCurrentSun());
    }

    public Result showTileStatus(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("tileX"));
        int y = Integer.parseInt(matcher.group("tileY"));

        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
            return new Result("Invalid coordinates.");
        }

        Tile tile = context.getMap().getMap()[y][x];
        StringBuilder status = new StringBuilder("Tile Status at (").append(x).append(",").append(y).append("):");

        if (tile.getTags().isEmpty())
            status.append("\n  Tags: Normal");
        else {
            status.append("\n  Tags:");
            for (TileTags tag : tile.getTags()) {
                status.append("\n    ").append(tag.toString());
            }
        }

        if (!tile.getBehaviors().isEmpty()) {
            status.append("\n  Status:");
            for (TileBehavior tb : tile.getBehaviors()) {
                status.append(tb.getStatus());
            }
        }

        if (tile.getPlants() != null && !tile.getPlants().isEmpty()) {
            status.append("\n").append("  Plant: ").append(tile.getPlants().getLast().getType());
        } else {
            status.append("\n").append("  Plant: None");
        }

        if (!context.getZombiesAt(x, y).isEmpty()) {
            status.append("\n").append("  Zombies: ");
            for (Zombie z : context.getZombiesAt(x, y)) {
                status.append("\n  ").append(z.getSheet().getAlias());
            }
        } else {
            status.append("\n").append("  Zombies: None");
        }

        return new Result(status.toString());
    }

    public Result showPlants(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Plants List:\n");
        int count = 0;
        int rows = context.getMap().getRows();
        int cols = context.getMap().getColumns();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile tile = context.getMap().getMap()[i][j];
                if (tile.getPlants() != null && !tile.getPlants().isEmpty()) {
                    Plant plant = tile.getPlants().get(tile.getPlants().size() - 1);
                    if (plant != null && !plant.isDead()) {
                        String stateLabel = plant.getCurrentState() != null ? plant.getCurrentState().getLabel()
                                : "Unknown";

                        sb.append(String.format("- %s | Position: (%d, %d) | HP: %.0f/%.0f | State: %s\n",
                                plant.getSheet().getName(),
                                plant.getCol(),
                                plant.getLane(),
                                plant.getHp(),
                                plant.getMaxHp(),
                                stateLabel));
                        count++;
                    }
                }
            }
        }
        if (count == 0) {
            return new Result("No plants on the map.");
        }
        return new Result(sb.toString().trim());
    }

    public Result showZombies(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Zombies List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Zombie zombie) {
                sb.append("\n" + zombie.toInfoString());
                count++;
            }
        }
        if (count == 0) {
            return new Result("No zombies on the map.");
        }
        return new Result(sb.toString().trim());
    }

    public Result showProjectiles(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Projectiles List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Projectile projectile) {
                sb.append(String.format("- %s | Position: (%.1f, %.1f) | Damage: %.1f | State: Active\n",
                        projectile.getType(),
                        projectile.getX(),
                        projectile.getY(),
                        projectile.getDamage()));
                count++;
            }
        }
        if (count == 0) {
            return new Result("No projectiles on the map.");
        }
        return new Result(sb.toString().trim());
    }

    public Result showSuns(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Dropped Suns List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Sun sun) {
                if (!sun.isDone()) {
                    sb.append(String.format("- %s | Position: (%d, %d) | Sun Amount: %d | Expiry: %.1fs\n",
                            sun.getType(),
                            sun.getCol(),
                            sun.getLane(),
                            sun.getAmount(),
                            sun.getSecondsRemaining()));
                    count++;
                }
            }
        }
        if (count == 0) {
            return new Result("No suns on the map.");
        }
        return new Result(sb.toString().trim());
    }
*/
}
