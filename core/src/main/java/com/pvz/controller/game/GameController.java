package com.pvz.controller.game;

import com.google.gson.Gson;
import com.pvz.models.MatchSession;
import com.pvz.network.MessageType;
import com.pvz.network.NetworkClient;
import com.pvz.network.PlayerRole;
import com.pvz.network.game.GameAction;
import com.pvz.network.game.GameStateSync;
import com.pvz.network.game.GameSyncEnvelope;
import com.pvz.network.game.QuickChatMessage;
import com.pvz.network.game.RenderFrame;
import com.pvz.network.game.StickerMessage;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.game.State.ObjectiveScreen;
import com.pvz.controller.game.State.PanningBack;
import com.pvz.controller.game.State.Playing;
import com.pvz.controller.game.State.State;
import com.pvz.models.AppContext;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.entities.effects.LootDrop;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.data.ZombiePropertySheet;
import com.pvz.models.entities.zombies.data.ZombieRegistry;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelLoader;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.WavePhase;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.behaviors.WaterBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.games.modes.capabilities.ZombiePlacer;
import com.pvz.models.games.modes.variants.IZombieMode;
import com.pvz.models.games.modes.variants.IZombieLocalMode;
import com.pvz.models.games.modes.variants.ScoredMode;
import com.pvz.models.games.modes.variants.VaseBreakerMode;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.user.MyPlant;
import com.pvz.view.GameModesMenu;
import com.pvz.view.TravelLogMenu;
import com.pvz.view.game.GameScreen;
import com.pvz.view.game.GameOverPopup;
import com.pvz.view.game.GameWinPopup;
import com.pvz.view.game.PauseMenuPopup;
import com.pvz.view.game.PlantSelectModal;
import com.pvz.view.game.ScoredPopup;
import com.pvz.view.game.ui.ConveyorBeltUiModal;
import com.pvz.view.game.ui.GameUiModal;
import com.pvz.view.game.ui.IZombieOnlineUiModal;
import com.pvz.view.game.ui.IZombieLocalUiModal;
import com.pvz.view.game.ui.NormalUiModal;
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

    private GameContext ctx;
    private Level level;
    private PlantSelectModal plantSelectModal;
    private GameUiModal gameUiModal;
    private State state = new ObjectiveScreen(this);

    private GameRenderer renderer;

    private TextureRegion[] backgroundTextures;
    private boolean isIZombie = false;
    private float backgroundYOffset = 0f;

    // --- Phase 3: I,Zombie online sync -------------------------------------------
    private final Gson gson = new Gson();
    private final MatchSession matchSession = AppContext.getInstance().getMatchSession();
    private final boolean isNetworkedMatch = matchSession != null;
    /**
     * میزبان همیشه نقش گیاهه — چون کول‌داون واقعی کارت‌های گیاه فقط با اجرای
     * GameEngine تیک می‌خوره، و فقط میزبان GameEngine رو اجرا می‌کنه.
     */
    private final boolean isHost = isNetworkedMatch && matchSession.getMyRole() == PlayerRole.PLANT;
    private final GameStateSync.GuestMirrorState guestMirror = new GameStateSync.GuestMirrorState();
    private float snapshotAccumulator = 0f;
    private boolean lastGameOver = false;
    private static final float SNAPSHOT_INTERVAL_SECONDS = 0.01f;
    /**
     * Monotonic snapshot counter stamped onto every snapshot for stale/ordering
     * checks.
     */
    private int snapshotSeq = 0;
    /**
     * Highest seq the guest has applied so far (drops out-of-order/stale
     * snapshots).
     */
    private int lastAppliedSeq = -1;

    private final Vector3 touchPos = new Vector3();

    private float startX;
    private float endX;
    private boolean gameStarted = false;
    private Label readyPlantLabel;
    private Label errorMessageLabel;
    private float errorMessageTimer = 0f;
    private final float errorMessageDuration = 2.5f;

    private boolean paused = false;
    private Table pauseOverlay;
    private boolean cardsInitialized = false;

    /**
     * Decorative wave-1 zombies shown on the far right during the intro camera
     * pan (before the real GameContext exists). They loop their idle clip the
     * whole time; cleared when the camera returns to its starting spot.
     */
    private final List<DisplayZombie> displayZombies = new ArrayList<>();

    /**
     * Placement-preview ghost: plays the selected plant's idle PAM under the
     * cursor.
     */

    private PlantCard previewPlantCard;
    private ZombieCard previewZombieCard;

    /**
     * Split I,Zombie (local 2-player): the zombie side's keyboard-controlled
     * tile cursor and whether it is currently in "placing" mode. While placing,
     * arrows move this cursor and Enter spawns the armed zombie card there.
     */
    private boolean zombiePlacing = false;
    private int zombieCursorCol = 0;
    private int zombieCursorLane = 0;

    /** Shovel tool: translucent shovel icon that follows the mouse while armed. */
    private com.badlogic.gdx.scenes.scene2d.ui.Image shovelCursorPreview;
    private final com.badlogic.gdx.math.Vector2 shovelCursorPosition = new com.badlogic.gdx.math.Vector2();

    /**
     * Plant food tool: translucent plant-food icon that follows the mouse while
     * armed.
     */
    private com.badlogic.gdx.scenes.scene2d.ui.Image plantFoodCursorPreview;
    private final com.badlogic.gdx.math.Vector2 plantFoodCursorPosition = new com.badlogic.gdx.math.Vector2();

    public GameController(String seasonName, int levelNumber) {
        this.seasonName = seasonName;
        this.levelNumber = levelNumber;

        // Fresh session: wipe any state left over from a previously played level
        // so stale entities (zombies/plants/projectiles/context) never leak into
        // the new one before the real game session starts.
        GameEngine.getInstance().reset();
        AppContext.getInstance().setGameContext(null);

        this.batch = PvZ2.batch;
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        stage = new Stage(new FitViewport(1280, 720));

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (state instanceof Playing && !paused) {
                    if (ctx != null) {
                        // Clicks on the open quick-chat picker or sticker box never
                        // reach the battlefield — their own buttons handle them.
                        com.badlogic.gdx.math.Vector2 stageClick = stage.screenToStageCoordinates(
                                new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
                        if (gameUiModal instanceof com.pvz.view.game.ui.IZombieOnlineUiModal izombieUi
                                && ((izombieUi.isChatPickerVisible()
                                        && izombieUi.chatPickerContains(stageClick.x, stageClick.y))
                                        || (izombieUi.isStickerBoxVisible()
                                                && izombieUi.stickerBoxContains(stageClick.x, stageClick.y)))) {
                            return true;
                        }

                        // تبدیل ورودی ماوس/لمس به مختصات دقیق World
                        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(touchPos);

                        // If a zombie card is selected, prioritize zombie placement.
                        // In the local split mode the zombie side is keyboard-driven, so the
                        // mouse is the plant player and never routes clicks toward zombies.
                        boolean mouseZombieActive = ctx
                                .getMode() instanceof com.pvz.models.games.modes.variants.IZombieMode;
                        boolean zombieCardSelected = gameUiModal != null
                                && gameUiModal.getSelectedZombieCard() != null
                                && mouseZombieActive;

                        // Plant food: clicking a planted tile feeds the plant
                        if (gameUiModal != null && gameUiModal.isPlantFoodSelected()) {
                            if (checkPlantFoodClick(touchPos.x, touchPos.y)) {
                                gameUiModal.setPlantFoodSelected(false);
                                hidePlantFoodCursor();
                            }
                            return true;
                        }

                        // Shovel: clicking a planted tile digs the plant up (no sun refund)
                        if (gameUiModal != null && gameUiModal.isShovelSelected()) {
                            Tile shovelTile = ctx.getMap().getTileAt(touchPos.x, touchPos.y);
                            if (shovelTile != null) {
                                List<Plant> plantsAtTile = new java.util.ArrayList<>(
                                        ctx.getPlantsAt(shovelTile.getCol(), shovelTile.getLane()));
                                if (!plantsAtTile.isEmpty()) {
                                    Plant target = plantsAtTile.get(0);
                                    if (target != null && !target.isDead()) {
                                        ctx.removePlant(target);
                                        gameUiModal.setShovelSelected(false);
                                        hideShovelCursor();
                                        if (com.pvz.utils.DebugMode.isEnabled())
                                            Gdx.app.log("Shovel", "Dug up " + target.getType() + " (no sun refund).");
                                        return true;
                                    }
                                } else {
                                    // Clicked empty tile with shovel armed: keep it armed, but no error
                                    return true;
                                }
                            }
                            return true;
                        }

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
                            // پلنت‌فود آن را اجرا می‌کند
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
                                    } else if (card.getCost() > ctx.getCurrentSun()) {
                                        showErrorMessage("Not enough sun! You need "
                                                + card.getCost() + " sun (you have " + ctx.getCurrentSun() + ").");
                                    } else if (isAquaticPlant(card) && !isWaterTile(hoveredTile)) {
                                        String name = aquaticPlantName(card);
                                        showErrorMessage(name + " must be planted on water!");
                                    }
                                }
                            }
                        }

                        // ۲.۱. کاشت زامبی در صورت انتخاب کارت زامبی (IZombie).
                        // In the local split mode zombies are keyboard-only, never mouse.
                        if (gameUiModal != null && gameUiModal.getSelectedZombieCard() != null
                                && !(ctx.getMode() instanceof com.pvz.models.games.modes.variants.IZombieLocalMode)) {
                            Tile hoveredTile = ctx.getMap().getTileAt(touchPos.x, touchPos.y);
                            if (hoveredTile != null) {
                                ZombieCard zCard = gameUiModal.getSelectedZombieCard();
                                if (ctx.getMode() instanceof ZombiePlacer placer) {
                                    int col = hoveredTile.getCol();
                                    int lane = hoveredTile.getLane();

                                    if (placer.isValidPlacement(ctx, col, lane, zCard)) {
                                        if (isNetworkedMatch && !isHost) {
                                            if (ctx.getMode() instanceof IZombieMode izMode) {
                                                izMode.spendZombieSun(zCard.getCost());
                                            }
                                            sendPlacementAction(GameAction.Type.PLACE_ZOMBIE,
                                                    zCard.getZombieType().name(), col, lane);
                                        } else {
                                            placer.handlePlacement(ctx, col, lane, zCard);
                                        }
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

        gameUiModal = switch (level.getGameMode()) {
            case com.pvz.models.games.modes.GameModeType.CONVEYORBELT -> new ConveyorBeltUiModal(this::pauseGame);
            case com.pvz.models.games.modes.GameModeType.SPLIT_IZOMBIE ->
                new com.pvz.view.game.ui.IZombieLocalUiModal(this::pauseGame);
            case com.pvz.models.games.modes.GameModeType.IZOMBIE ->
                new IZombieOnlineUiModal(this::pauseGame, this::sendQuickChat, this::sendSticker);
            default -> new NormalUiModal(this::pauseGame);
        };
        gameUiModal.setOnShovelRequested(this::toggleShovelMode);
        gameUiModal.setOnPlantFoodRequested(this::togglePlantFoodMode);
        stage.addActor(gameUiModal);

        backgroundTextures = new TextureRegion[3];
        switch (seasonName.toLowerCase()) {
            case "ancient egypt" -> {
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_RIGHT");
            }
            case "frostbite caves" -> {
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_ICEAGE_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_ICEAGE_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_ICEAGE_TEXTURE_RIGHT");
                backgroundYOffset = -17f;
            }
            case "dark ages" -> {
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_DARK_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_DARK_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_DARK_TEXTURE_RIGHT");
            }
            case "big wave beach" -> {
                backgroundTextures[0] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_BEACH_TEXTURE_LEFT");
                backgroundTextures[1] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_BEACH_TEXTURE");
                backgroundTextures[2] = PvZ2.textureBank.region("IMAGE_BACKGROUNDS_BEACH_TEXTURE_RIGHT");
            }
            case "izombie" -> {
                isIZombie = true;
                com.badlogic.gdx.graphics.Texture left = new com.badlogic.gdx.graphics.Texture(
                        Gdx.files.internal("textures/backgrounds/IZOMBIE/texture_left.png"));
                com.badlogic.gdx.graphics.Texture mid = new com.badlogic.gdx.graphics.Texture(
                        Gdx.files.internal("textures/backgrounds/IZOMBIE/texture.png"));
                com.badlogic.gdx.graphics.Texture right = new com.badlogic.gdx.graphics.Texture(
                        Gdx.files.internal("textures/backgrounds/IZOMBIE/texture_right.png"));
                backgroundTextures[0] = new TextureRegion(left);
                backgroundTextures[1] = new TextureRegion(mid);
                backgroundTextures[2] = new TextureRegion(right);
            }
            case "splitizombie" -> {
                isIZombie = true;
                com.badlogic.gdx.graphics.Texture left = new com.badlogic.gdx.graphics.Texture(
                        Gdx.files.internal("textures/backgrounds/IZOMBIE/texture_left.png"));
                com.badlogic.gdx.graphics.Texture mid = new com.badlogic.gdx.graphics.Texture(
                        Gdx.files.internal("textures/backgrounds/IZOMBIE/texture.png"));
                com.badlogic.gdx.graphics.Texture right = new com.badlogic.gdx.graphics.Texture(
                        Gdx.files.internal("textures/backgrounds/IZOMBIE/texture_right.png"));
                backgroundTextures[0] = new TextureRegion(left);
                backgroundTextures[1] = new TextureRegion(mid);
                backgroundTextures[2] = new TextureRegion(right);
            }
            default -> {
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
        labelTable.add(readyPlantLabel).padTop(-80).row();
        stage.addActor(labelTable);

        errorMessageLabel = new Label("", PvzSkin.get(), "medium");
        errorMessageLabel.setColor(Color.RED);
        errorMessageLabel.setFontScale(1.2f);
        errorMessageLabel.setAlignment(Align.center);
        errorMessageLabel.setVisible(false);
        errorMessageLabel.setWrap(true);
        errorMessageLabel.setWidth(600f);

        Table errorTable = new Table();
        errorTable.setFillParent(true);
        errorTable.top();
        errorTable.add(errorMessageLabel).padTop(80).width(600f);
        stage.addActor(errorTable);

        renderer = new GameRenderer(ctx, this, batch, backgroundTextures);
        createDisplayZombies();
        state.enter();
    }

    public void changeState(State state) {
        this.state.exit();
        this.state = state;
        state.enter();
    }

    /** The logged-in user's Game Speed setting (1/2/3), clamped to a safe range. */
    public int getGameSpeed() {
        com.pvz.models.user.User user = AppContext.getInstance().getCurrentUser();
        int speed = 1;
        if (user != null && user.getSetting() != null) {
            speed = user.getSetting().getGameSpeed();
        }
        return Math.max(1, Math.min(3, speed));
    }

    public void update(float dt) {
        camera.update();
        updateShovelCursorPreview();
        updatePlantFoodCursorPreview();

        float simDt = dt * getGameSpeed();

        if (errorMessageLabel != null && errorMessageLabel.isVisible()) {
            errorMessageTimer += dt;
            if (errorMessageTimer >= errorMessageDuration) {
                errorMessageLabel.setVisible(false);
            }
        }

        if (com.pvz.utils.DebugMode.isEnabled() && Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            renderer.toggleShowHitBoxes();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (paused) {
                resumeGame();
            } else if (state instanceof Playing) {
                pauseGame();
            }
        }

        handleSplitIZombieKeyboardUpdate(dt);

        state.update(state instanceof Playing ? simDt : dt);

        // Check sun & coin-drop clicks in PLAYING state on left click (only when no
        // card selected)
        if (state instanceof Playing && !paused
                && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            boolean zombieCardActive = gameUiModal != null && gameUiModal.getSelectedZombieCard() != null;
            boolean onPopup = false;
            com.pvz.view.game.ui.IZombieOnlineUiModal izombieUi =
                    gameUiModal instanceof com.pvz.view.game.ui.IZombieOnlineUiModal m
                    ? m
                    : null;
            if (izombieUi != null) {
                com.badlogic.gdx.math.Vector2 uClick = stage.screenToStageCoordinates(
                        new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
                onPopup = (izombieUi.isChatPickerVisible() && izombieUi.chatPickerContains(uClick.x, uClick.y))
                        || (izombieUi.isStickerBoxVisible() && izombieUi.stickerBoxContains(uClick.x, uClick.y));
            }
            if (!zombieCardActive && !onPopup) {
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
        if (state instanceof Playing && !paused) {
            if (!isNetworkedMatch || isHost) {
                GameEngine.getInstance().update(simDt);
            } else if (ctx != null) {
                // Guest: no entity simulation for rendering — the drawn picture (a
                // list of FrameConfigs) arrives from the host each frame. But the
                // zombie player's own sun economy (sun producers minting collectible
                // zombie suns) stays local so they can afford zombies.
                if (ctx.getMode() instanceof IZombieMode izMode) {
                    izMode.ensureGuestSunProducers(ctx);
                    izMode.updateSunProducers(ctx, simDt);
                }
                for (Sun s : new ArrayList<>(ctx.getSuns())) {
                    if (s.isDone()) {
                        ctx.removeSun(s);
                    }
                }
                ctx.flushPending();
                // Cards still need their cooldown overlays ticked for the HUD.
                GameStateSync.tickCardsOnly(ctx, simDt);
            }

            if (isNetworkedMatch && isHost) {
                boolean gameOverNow = ctx.isGameOver();
                snapshotAccumulator += dt;
                boolean forceSend = gameOverNow && !lastGameOver;
                lastGameOver = gameOverNow;
                if (forceSend || snapshotAccumulator >= SNAPSHOT_INTERVAL_SECONDS) {
                    snapshotAccumulator = 0f;
                    sendRenderFrame(forceSend);
                }
            }

            if (!cardsInitialized) {
                cardsInitialized = true;
                gameUiModal.initCards();
                gameUiModal.setVisible(true);
            }
        }

        renderer.update(state instanceof Playing ? simDt : dt);

        pollScoredPopups(dt);
    }

    /**
     * Polls the scored game mode for newly earned miopoint awards and displays a
     * transient pop-up ("You scored N points!") for each one.
     */
    private void pollScoredPopups(float dt) {
        if (ctx == null) {
            return;
        }
        if (!(ctx.getMode() instanceof ScoredMode scoredMode)) {
            return;
        }
        List<ScoredMode.ScoredEvent> events = scoredMode.drainEvents();
        for (ScoredMode.ScoredEvent event : events) {
            ScoredPopup popup = new ScoredPopup(event.points);
            stage.addActor(popup);
            popup.toFront();
        }
    }

    // ---- Split I,Zombie (local 2-player): zombie-side keyboard input ----

    private void handleSplitIZombieKeyboardUpdate(float dt) {
        if (ctx == null || !(state instanceof Playing) || paused)
            return;
        if (!(ctx.getMode() instanceof IZombieLocalMode izMode))
            return;
        if (!(gameUiModal instanceof IZombieLocalUiModal izUi))
            return;

        // S: collect every zombie-owned sun on the board.
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            collectAllZombieSuns(izMode);
        }

        if (zombiePlacing) {
            // Placing mode: arrows move the tile cursor, Enter spawns.
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                zombieCursorLane = Math.max(0, zombieCursorLane - 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                zombieCursorLane = Math.min(ctx.getMap().getLanes() - 1, zombieCursorLane + 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                zombieCursorCol = Math.max(izMode.getRedLineColumn(), zombieCursorCol - 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                zombieCursorCol = Math.min(ctx.getMap().getColumns() - 1, zombieCursorCol + 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                placeZombieAtCursor(izMode, izUi);
            }
        } else {
            // Selection mode: up/down cycle the highlighted zombie card.
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                izUi.moveZombieSelection(-1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                izUi.moveZombieSelection(1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                enterZombiePlacing(izMode, izUi);
            }
        }
    }

    /** Arms the currently highlighted zombie card and opens tile-placement mode. */
    private void enterZombiePlacing(IZombieLocalMode izMode, IZombieLocalUiModal izUi) {
        if (izUi.getZombieCardCount() == 0)
            return;
        izUi.armSelectedZombie();
        // Park the cursor on a legal placement tile (right of the red line).
        zombieCursorCol = Math.max(zombieCursorCol, izMode.getRedLineColumn());
        zombieCursorLane = MathUtils.clamp(zombieCursorLane, 0, ctx.getMap().getLanes() - 1);
        zombiePlacing = true;
    }

    /** Spawns the armed zombie at the cursor tile, then exits placing mode. */
    private void placeZombieAtCursor(IZombieLocalMode izMode, IZombieLocalUiModal izUi) {
        ZombieCard zCard = izUi.getSelectedZombieCard();
        if (zCard != null && ctx.getMode() instanceof ZombiePlacer placer) {
            int col = zombieCursorCol;
            int lane = zombieCursorLane;
            if (placer.isValidPlacement(ctx, col, lane, zCard)) {
                placer.handlePlacement(ctx, col, lane, zCard);
            } else if (izMode.getZombieSun() < zCard.getCost()) {
                showErrorMessage("Zombie side needs " + zCard.getCost() + " sun (you have "
                        + izMode.getZombieSun() + ").");
            } else {
                showErrorMessage("Can't place a zombie there.");
            }
        }
        zombiePlacing = false;
    }

    /** Collects every zombie-owned sun into the zombie player's pool. */
    private void collectAllZombieSuns(IZombieLocalMode izMode) {
        if (ctx == null)
            return;
        boolean any = false;
        for (Sun sun : new ArrayList<>(ctx.getSuns())) {
            if (sun.isDone() || sun.isStealing())
                continue;
            if (sun.getOwner() == Sun.SunOwner.ZOMBIE) {
                izMode.addZombieSun(sun.getAmount());
                // Collect into the zombie pool only — sun.collect(ctx) would also
                // credit the plant-side pool, so just drop the sun entity instead.
                ctx.removeSun(sun);
                any = true;
            }
        }
        if (com.pvz.utils.DebugMode.isEnabled() && any) {
            Gdx.app.log("SplitIZombie", "Collected zombie suns, pool now " + izMode.getZombieSun());
        }
    }

    /** Whether the zombie side is currently in tile-placement mode. */
    public boolean isZombiePlacing() {
        return zombiePlacing && ctx != null
                && (ctx.getMode() instanceof IZombieLocalMode);
    }

    public int getZombieCursorCol() {
        return zombieCursorCol;
    }

    public int getZombieCursorLane() {
        return zombieCursorLane;
    }

    private void pauseGame() {
        if (!(state instanceof Playing) || paused) {
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

    public void showGameEndPopup() {
        paused = true;

        var user = com.pvz.models.AppContext.getInstance().getCurrentUser();
        if (user != null) {
            syncPlantFoodToProfile(user);
            user.saveUser();
        }

        boolean won;
        if (ctx.getMode() instanceof IZombieMode izMode) {
            IZombieMode.Outcome outcome = izMode.getOutcome();
            if (isNetworkedMatch) {
                PlayerRole myRole = matchSession.getMyRole();
                won = (outcome == IZombieMode.Outcome.ZOMBIES_WIN && myRole == PlayerRole.ZOMBIE)
                        || (outcome == IZombieMode.Outcome.PLANTS_WIN && myRole == PlayerRole.PLANT);
            } else {
                won = (outcome == IZombieMode.Outcome.ZOMBIES_WIN);
            }
        } else if (ctx.getMode() instanceof IZombieLocalMode izLocalMode) {
            won = (izLocalMode.getOutcome() == IZombieLocalMode.Outcome.ZOMBIES_WIN);
        } else if (ctx.getMode() instanceof com.pvz.models.games.modes.variants.TimedWarMode twMode) {
            won = twMode.getOutcome() == com.pvz.models.games.modes.variants.TimedWarMode.Outcome.VICTORY;
        } else {
            won = ctx.getZombies().isEmpty();
        }

        // Scored (mini-point) mode: record the miopoint total on the user.
        if (ctx.getMode() instanceof ScoredMode scoredMode && user != null) {
            int miopoints = scoredMode.getTotalMiopoints();
            recordMiopoints(user, miopoints);
        }

        if (user != null && user.getQuestLog() != null && ctx.getGameStats() != null) {
            com.pvz.models.quests.QuestEvaluator.evaluateAll(
                    user.getQuestLog(), ctx.getGameStats(), won,
                    ctx.getCurrentSun(), 1, ctx);
            user.saveUser();
        }

        Runnable exitAction = () -> Gdx.app.postRunnable(() -> {
            if (ctx.getMode() instanceof IZombieMode
                    || ctx.getMode() instanceof IZombieLocalMode
                    || ctx.getMode() instanceof ScoredMode) {
                PvZ2.instance.setScreen(new TravelLogMenu(PvZ2.instance));
            } else {
                PvZ2.instance.setScreen(new GameModesMenu(PvZ2.instance));
            }
        });

        Table popup;
        if (won) {
            String title = "Level Complete!";
            String msg = "You defeated the zombies!";
            if (ctx.getMode() instanceof IZombieMode izMode) {
                if (isNetworkedMatch && matchSession.getMyRole() == PlayerRole.PLANT) {
                    title = "Level Complete!";
                    msg = "You survived the zombie onslaught!";
                } else {
                    title = "I, ZOMBIE COMPLETE!";
                    msg = "All five brains were eaten!";
                }
            } else if (ctx.getMode() instanceof IZombieLocalMode) {
                title = "ZOMBIES WIN!";
                msg = "All five brains were eaten!";
            } else if (ctx.getMode() instanceof ScoredMode scoredWinMode) {
                title = "ALL WAVES CLEARED!";
                msg = "You earned " + scoredWinMode.getTotalMiopoints() + " miopoints!";
            }
            Runnable nextAction;
            if (ctx.getMode() instanceof IZombieLocalMode || ctx.getMode() instanceof ScoredMode) {
                // Single level: no "next level" — head straight back to the map.
                nextAction = exitAction;
            } else {
                nextAction = () -> Gdx.app.postRunnable(() -> {
                    if (isNetworkedMatch) {
                        AppContext.getInstance().setMatchSession(null);
                        PvZ2.instance
                                .setScreen(new com.pvz.view.OpponentSelectMenu(PvZ2.instance, seasonName, levelNumber));
                    } else {
                        PvZ2.instance.setScreen(new GameScreen(seasonName, levelNumber + 1));
                    }
                });
            }
            popup = new GameWinPopup(title, msg, "EXIT TO MAP", exitAction, "NEXT LEVEL", nextAction);
        } else {
            String title = "THE ZOMBIES\nATE YOUR\nBRAINS!";
            if (ctx.getMode() instanceof IZombieMode) {
                if (isNetworkedMatch && matchSession.getMyRole() == PlayerRole.ZOMBIE) {
                    title = "THE ZOMBIES\nCOULD NOT EAT\nTHE BRAINS!";
                }
            } else if (ctx.getMode() instanceof IZombieLocalMode) {
                title = "THE ZOMBIES\nCOULD NOT EAT\nTHE BRAINS!";
            }
            Runnable retryAction = () -> Gdx.app.postRunnable(() -> {
                if (isNetworkedMatch) {
                    AppContext.getInstance().setMatchSession(null);
                    PvZ2.instance
                            .setScreen(new com.pvz.view.OpponentSelectMenu(PvZ2.instance, seasonName, levelNumber));
                } else {
                    PvZ2.instance.setScreen(new GameScreen(seasonName, levelNumber));
                }
            });
            popup = new GameOverPopup(title, "EXIT TO MAP", exitAction, "RETRY", retryAction);
        }

        stage.addActor(popup);
        popup.toFront();
    }

    private void syncPlantFoodToProfile(com.pvz.models.user.User user) {
        if (ctx != null && user.getProfile() != null) {
            int synced = Math.min(ctx.getProfilePlantFoodOnEntry(), ctx.getPlantFoodCount());
            user.getProfile().setPlantFood(synced);
        }
    }

    /**
     * Records the miopoints earned in the scored (mini-point) mode onto the
     * user's persistent record: updates {@code Score.highestScore} and the
     * profile's best miopoint, then persists the user (via the network when
     * connected, otherwise to a local JSON file).
     */
    private void recordMiopoints(com.pvz.models.user.User user, int miopoints) {
        if (user == null || user.getScore() == null || user.getProfile() == null) {
            return;
        }
        int best = Math.max(user.getScore().getHighestScore(), miopoints);
        user.getScore().setHighestScore(best);
        user.getScore().setBestMiopoint(Math.max(user.getScore().getBestMiopoint(), miopoints));
        if (miopoints > user.getProfile().getMaxMiopoint()) {
            user.getProfile().setMaxMiopoint(miopoints);
        }
        user.saveUser();
    }

    private void saveAndExit() {
        resumeGame();
        var user = com.pvz.models.AppContext.getInstance().getCurrentUser();
        if (user != null) {
            syncPlantFoodToProfile(user);
            user.saveUser();
        }
        if (isNetworkedMatch) {
            NetworkClient.getInstance().setDisconnectListener(null);
            NetworkClient.getInstance().clearPushListener(MessageType.OPPONENT_DISCONNECTED);
            NetworkClient.getInstance().clearPushListener(MessageType.MATCH_MESSAGE);
            NetworkClient.getInstance().sendMatchMessage(matchSession.getMatchId(),
                    gson.toJson(new GameSyncEnvelope(GameSyncEnvelope.Kind.QUIT, null)));
        }
        AppContext.getInstance().setMatchSession(null);
        Gdx.app.postRunnable(() -> {
            if (ctx != null && (ctx.getMode() instanceof IZombieMode
                    || ctx.getMode() instanceof IZombieLocalMode)) {
                PvZ2.instance.setScreen(new TravelLogMenu(PvZ2.instance));
            } else {
                PvZ2.instance.setScreen(new GameModesMenu(PvZ2.instance));
            }
        });
    }

    public void resize(int width, int hight) {
        viewport.update(width, hight);
        stage.getViewport().update(width, hight);
    }

    private void showErrorMessage(String message) {
        if (errorMessageLabel == null)
            return;
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
        errorMessageTimer = 0f;
        errorMessageLabel.toFront();
    }

    private boolean isAquaticPlant(PlantCard card) {
        if (card == null)
            return false;
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        return sheet != null && sheet.getTags().contains(PlantTag.WATER);
    }

    private String aquaticPlantName(PlantCard card) {
        if (card == null)
            return "This plant";
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        return sheet != null ? sheet.getName() : card.getPlant().getType().toString();
    }

    private boolean isWaterTile(Tile tile) {
        if (tile == null)
            return false;
        return tile.getTags().contains(TileTags.WATER)
                || tile.getBehaviors().stream().anyMatch(b -> b instanceof WaterBehavior);
    }

    /**
     * Toggles the shovel tool. Selecting it clears any armed plant/zombie card
     * and shows a translucent shovel cursor following the mouse; clicking a
     * planted tile then digs the plant up (refunding its sun cost). Clicking
     * the shovel again disarms it. Mirrors the phase-0 reference behaviour.
     */
    private void toggleShovelMode() {
        if (gameUiModal == null)
            return;
        boolean willSelect = !gameUiModal.isShovelSelected();
        gameUiModal.setShovelSelected(willSelect);
        if (willSelect) {
            showShovelCursor();
        } else {
            hideShovelCursor();
        }
    }

    private void showShovelCursor() {
        com.badlogic.gdx.graphics.g2d.TextureRegion region = PvZ2.textureBank.region("IMAGE_UI_HUD_INGAME_SHOVEL_ICON");
        if (region == null) {
            Gdx.app.error("GameController", "Shovel cursor region not found.");
            return;
        }
        if (shovelCursorPreview == null) {
            shovelCursorPreview = new com.badlogic.gdx.scenes.scene2d.ui.Image();
            shovelCursorPreview.setTouchable(Touchable.disabled);
            stage.addActor(shovelCursorPreview);
        }
        shovelCursorPreview.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(region));
        shovelCursorPreview.setSize(region.getRegionWidth() * 0.65f, region.getRegionHeight() * 0.65f);
        shovelCursorPreview.setColor(1f, 1f, 1f, 0.58f);
        shovelCursorPreview.setVisible(true);
        shovelCursorPreview.toFront();
    }

    private void hideShovelCursor() {
        if (shovelCursorPreview != null) {
            shovelCursorPreview.setVisible(false);
        }
    }

    private void updateShovelCursorPreview() {
        if (shovelCursorPreview == null || !shovelCursorPreview.isVisible())
            return;
        shovelCursorPosition.set(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(shovelCursorPosition);
        shovelCursorPreview.setPosition(
                shovelCursorPosition.x - shovelCursorPreview.getWidth() / 2f,
                shovelCursorPosition.y - shovelCursorPreview.getHeight() / 2f);
        shovelCursorPreview.toFront();
    }

    /**
     * Toggles the plant food tool. Clicking the plant food HUD display arms
     * the tool (shows a translucent plant-food icon following the mouse);
     * clicking a planted tile then feeds that plant. Clicking the HUD display
     * again disarms it. Mirrors the phase-0 reference behaviour.
     */
    private void togglePlantFoodMode() {
        if (gameUiModal == null || ctx == null)
            return;
        boolean willSelect = !gameUiModal.isPlantFoodSelected();
        if (willSelect && ctx.getPlantFoodCount() <= 0) {
            showErrorMessage("You do not have any Plant Food.");
            return;
        }
        gameUiModal.setPlantFoodSelected(willSelect);
        if (willSelect) {
            gameUiModal.setSelectedCard(null);
            showPlantFoodCursor();
        } else {
            hidePlantFoodCursor();
        }
    }

    private void showPlantFoodCursor() {
        com.badlogic.gdx.graphics.g2d.TextureRegion region = PvZ2.textureBank
                .region("IMAGE_EFFECTS_PLANTFOOD_PICKUP_PLANTFOOD_PICKUP_79X79");
        if (region == null) {
            region = PvZ2.textureBank.region("IMAGE_UI_ALMANAC_PLANT_FOOD_STAT_ICON");
        }
        if (region == null) {
            Gdx.app.error("GameController", "Plant food cursor region not found.");
            return;
        }
        if (plantFoodCursorPreview == null) {
            plantFoodCursorPreview = new com.badlogic.gdx.scenes.scene2d.ui.Image();
            plantFoodCursorPreview.setTouchable(Touchable.disabled);
            stage.addActor(plantFoodCursorPreview);
        }
        plantFoodCursorPreview.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(region));
        plantFoodCursorPreview.setSize(region.getRegionWidth() * 1.2f, region.getRegionHeight() * 1.2f);
        plantFoodCursorPreview.setColor(1f, 1f, 1f, 0.58f);
        plantFoodCursorPreview.setVisible(true);
        plantFoodCursorPreview.toFront();
    }

    private void hidePlantFoodCursor() {
        if (plantFoodCursorPreview != null) {
            plantFoodCursorPreview.setVisible(false);
        }
    }

    private void updatePlantFoodCursorPreview() {
        if (plantFoodCursorPreview == null || !plantFoodCursorPreview.isVisible())
            return;
        plantFoodCursorPosition.set(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(plantFoodCursorPosition);
        plantFoodCursorPreview.setPosition(
                plantFoodCursorPosition.x - plantFoodCursorPreview.getWidth() / 2f,
                plantFoodCursorPosition.y - plantFoodCursorPreview.getHeight() / 2f);
        plantFoodCursorPreview.toFront();
    }

    /**
     * Builds the decorative wave-1 zombies that stand on the far right (two
     * columns past the last ground column) during the intro camera pan. One per
     * lane, cycling through the distinct zombie types defined in wave 1's phases.
     * They are purely visual — created before the real GameContext exists and
     * cleared in {@link #startGameSession()}.
     */
    private void createDisplayZombies() {
        displayZombies.clear();
        try {
            List<Wave> waves = level != null ? level.getWaves() : java.util.Collections.emptyList();
            if (waves == null || waves.isEmpty())
                return;

            int lanes = level.getGameMapDefinition().rows;
            if (lanes <= 0)
                lanes = com.pvz.models.Constants.DEFAULT_ROWS;

            List<ZombieType> types = new ArrayList<>();
            java.util.Set<ZombieType> seen = new java.util.LinkedHashSet<>();
            for (WavePhase phase : waves.get(0).getPhases()) {
                if (phase.getAllowedTypes() == null)
                    continue;
                for (ZombieType t : phase.getAllowedTypes()) {
                    if (t != null && seen.add(t)) {
                        types.add(t);
                    }
                }
            }
            if (types.isEmpty())
                return;

            int col = level.getGameMapDefinition().columns + 2; // two columns past the last ground column
            float x = colToWorldX(col);
            for (int lane = 0; lane < lanes; lane++) {
                ZombieType type = types.get(lane % types.size());
                if (type.getAlias() == null)
                    continue;
                ZombiePropertySheet sheet = ZombieRegistry.getInstance().getSheet(type.getAlias());
                if (sheet == null)
                    continue;
                ZombieAnimationConfig anim = sheet.getAnimationConfig();
                String pamPath = (anim != null && anim.pamFilePath != null)
                        ? anim.pamFilePath
                        : "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM";
                float scale = (anim != null && anim.scale != null) ? anim.scale : 0.65f;
                String idle = (anim != null && anim.idleLabel != null) ? anim.idleLabel : "idle";
                displayZombies.add(new DisplayZombie(pamPath, idle, x, laneToWorldY(lane), scale));
            }
        } catch (Exception e) {
            if (com.pvz.utils.DebugMode.isEnabled()) {
                Gdx.app.log("GameController", "Failed to create display zombies", e);
            }
            displayZombies.clear();
        }
    }

    public void startGameSession() {
        try {
            if (level != null) {
                GameEngine.getInstance().reset();
                GameContext newContext = new GameContext(level);
                boolean guestIZombie = isNetworkedMatch && !isHost
                        && level.getGameMode() == com.pvz.models.games.modes.GameModeType.IZOMBIE;
                for (PlantType pt : plantSelectModal.getSelectedPlants()) {
                    if (guestIZombie)
                        break;
                    MyPlant owned = null;
                    try {
                        owned = AppContext.getInstance().getCurrentUser().getProfile().getCollection().getPlant(pt);
                    } catch (Exception ignored) {
                    }
                    PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(pt);

                    MyPlant myPlant = owned;
                    if (myPlant == null) {
                        myPlant = new MyPlant();
                        myPlant.setType(pt);
                        myPlant.setLevel(1);
                    }

                    // Resolve sun cost / recharge at the player's plant level so the card
                    // shows the same values the mode actually charges (level-up SUN_COST
                    // / RECHARGE_SECONDS modifiers are applied here, not on the sheet).
                    PlantStatResolver.ResolvedStats stats = (sheet != null)
                            ? PlantStatResolver.resolve(sheet, myPlant.getLevel())
                            : null;
                    int sunCost = (stats != null) ? stats.getSunCost() : ((sheet != null) ? sheet.getSunCost() : 50);
                    float recharge = (stats != null && stats.getRechargeSeconds() != null)
                            ? stats.getRechargeSeconds()
                            : ((sheet != null) ? sheet.getRechargeSeconds() : 5f);

                    newContext.addCard(new PlantCard(myPlant, sunCost, recharge));
                }
                AppContext.getInstance().setGameContext(newContext);
                ctx = newContext;
                if (isNetworkedMatch && !isHost) {
                    // Guest never runs GameEngine#update, so GameContext#enter() (which is
                    // what actually calls mode.initMode()) never fires the way it does for
                    // the host via the engine's newly-registered-entity processing.
                    // Flag networkGuest first so initMode only builds the zombie-side cards,
                    // then call enter() once, by hand, here.
                    if (ctx.getMode() instanceof IZombieMode izMode) {
                        izMode.setNetworkGuest(true);
                    }
                    ctx.enter();
                }
                if (isNetworkedMatch && isHost && ctx.getMode() instanceof IZombieMode izMode) {
                    izMode.setNetworkHost(true);
                }
                renderer.setContext(newContext);
                if (isNetworkedMatch && isHost) {
                    renderer.setHideZombieSuns(true);
                }

                plantSelectModal.setVisible(false);
                changeState(new PanningBack(this));
                Gdx.input.setInputProcessor(stage);

                if (isNetworkedMatch) {
                    NetworkClient.getInstance().onMatchMessage(this::handleMatchMessage);
                    NetworkClient.getInstance().setPushListener(MessageType.OPPONENT_DISCONNECTED,
                            msg -> handleDisconnect());
                    NetworkClient.getInstance().setDisconnectListener(() -> handleDisconnect());
                }

                if (com.pvz.utils.DebugMode.isEnabled())
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
                if (sun.isDone() || sun.isStealing())
                    continue;
                float sunX = sun.getX();
                float sunY = sun.getY();
                float dist = (float) Math.hypot(worldX - sunX, worldY - sunY);
                if (dist < 50f) {
                    if (isNetworkedMatch && !isHost) {
                        if (sun.getOwner() == Sun.SunOwner.ZOMBIE) {
                            if (ctx.getMode() instanceof IZombieMode izMode) {
                                izMode.addZombieSun(sun.getAmount());
                            }
                            sun.collect(ctx);
                        } else {
                            Integer hostSunId = guestMirror.idForSun(sun);
                            if (hostSunId != null) {
                                GameAction action = GameAction.collectSun(hostSunId);
                                String inner = gson.toJson(new GameSyncEnvelope(
                                        GameSyncEnvelope.Kind.ACTION, gson.toJson(action)));
                                NetworkClient.getInstance().sendMatchMessage(matchSession.getMatchId(), inner);
                            }
                        }
                    } else {
                        if (ctx.getMode() instanceof IZombieLocalMode izLocal
                                && sun.getOwner() == Sun.SunOwner.ZOMBIE) {
                            // Local split mode: zombie sun goes to the zombie pool,
                            // never to the plant-side context sun.
                            izLocal.addZombieSun(sun.getAmount());
                            ctx.removeSun(sun);
                        } else {
                            sun.collect(ctx);
                        }
                    }
                    if (com.pvz.utils.DebugMode.isEnabled())
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
        if (isNetworkedMatch && !isHost)
            return false; // simplification: guest doesn't collect loot this phase
        if (ctx == null) {
            return false;
        }
        for (LootDrop drop : new ArrayList<>(ctx.getLootDrops())) {
            if (drop.isDone() || !drop.isCollectable())
                continue;
            float dist = (float) Math.hypot(worldX - drop.getX(), worldY - drop.getY());
            if (dist < 55f) {
                if (drop.getType() == LootDrop.LootType.POT || drop.getType() == LootDrop.LootType.PLANT_FOOD) {
                    drop.collect();
                    if (com.pvz.utils.DebugMode.isEnabled())
                        Gdx.app.log("GameScreen", drop.getType().name() + " drop clicked, collected.");
                } else {
                    Vector2 target = lootWalletWorld(drop.getType());
                    drop.flyTo(target.x, target.y);
                    if (com.pvz.utils.DebugMode.isEnabled())
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
     * If the click lands inside a planted plant's hitbox and the player has
     * plant food available, triggers the plant's Plant Food effect and
     * consumes one plant food.
     *
     * @return {@code true} if the click was consumed by a plant food
     */
    private boolean checkPlantFoodClick(float worldX, float worldY) {
        if (ctx == null) {
            return false;
        }

        Plant target = null;
        int hitCol = -1;
        int hitLane = -1;

        // Resolve the clicked tile first so stacked plants (e.g. a LilyPad with a
        // plant on top) are handled correctly. Every plant on a tile shares the same
        // full-tile hitbox, so we prefer the plant drawn on top (last in the tile
        // list) that isn't a LilyPad, and fall back to the sole occupant otherwise.
        Tile tile = ctx.getMap().getTileAt(worldX, worldY);
        if (tile != null) {
            hitCol = tile.getCol();
            hitLane = tile.getLane();
            List<Plant> plantsOnTile = ctx.getPlantsAt(hitCol, hitLane);
            for (Plant p : plantsOnTile) {
                if (p.isDead()) {
                    continue;
                }
                if (p.getType() == PlantType.LilyPad) {
                    continue;
                }
                target = p;
            }
            if (target == null && !plantsOnTile.isEmpty()) {
                target = plantsOnTile.get(plantsOnTile.size() - 1);
            }
        }

        // Fallback: if the tile doesn't map to any plant, scan the global list.
        if (target == null) {
            for (Plant plant : new ArrayList<>(ctx.getPlants())) {
                if (plant.isDead()) {
                    continue;
                }
                if (plant.getHitbox() != null
                        && plant.getHitbox().getRectangle().contains(worldX, worldY)) {
                    target = plant;
                    break;
                }
            }
        }

        if (target == null) {
            return false;
        }

        if (!ctx.spendPlantFood()) {
            ctx.log("[PlantFood] No plant food available!");
            return false;
        }
        target.triggerPlantFood(ctx);
        ctx.log("[PlantFood] " + target.getSheet().getName()
                + " at (" + target.getCol() + "," + target.getLane() + ") used Plant Food!");
        return true;
    }

    public GameContext getCtx() {
        return ctx;
    }

    public PlantSelectModal getPlantSelectModal() {
        return plantSelectModal;
    }

    public GameUiModal getGameUiModal() {
        return gameUiModal;
    }

    public State getState() {
        return state;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Stage getStage() {
        return stage;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public Level getLevel() {
        return level;
    }

    public float getStartX() {
        return startX;
    }

    public float getEndX() {
        return endX;
    }

    public List<DisplayZombie> getDisplayZombies() {
        return displayZombies;
    }

    /** Removes the decorative intro zombies. */
    public void clearDisplayZombies() {
        displayZombies.clear();
    }

    public Label getReadyPlantLabel() {
        return readyPlantLabel;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isPaused() {
        return paused;
    }

    private void handleMatchMessage(com.pvz.network.MatchDTOs.MatchMessageEnvelope wrapper) {
        if (wrapper == null || wrapper.payload == null)
            return;

        GameSyncEnvelope envelope = gson.fromJson(wrapper.payload, GameSyncEnvelope.class);
        if (envelope == null)
            return;

        if (envelope.kind == GameSyncEnvelope.Kind.QUIT) {
            handleDisconnect();
        } else if (envelope.kind == GameSyncEnvelope.Kind.CHAT) {
            QuickChatMessage chat = gson.fromJson(envelope.data, QuickChatMessage.class);
            if (gameUiModal instanceof IZombieOnlineUiModal izombieUi) {
                izombieUi.showChat(chat);
            }
        } else if (envelope.kind == GameSyncEnvelope.Kind.STICKER) {
            StickerMessage sticker = gson.fromJson(envelope.data, StickerMessage.class);
            if (gameUiModal instanceof IZombieOnlineUiModal izombieUi) {
                izombieUi.showSticker(sticker);
            }
        } else if (envelope.kind == GameSyncEnvelope.Kind.SNAPSHOT && !isHost) {
            // The SNAPSHOT payload is now a compact binary RenderFrame (base64) —
            // the full list of FrameConfigs the host is drawing. No entity
            // reconstruction needed; we just render these frames directly.
            handleRenderFrame(envelope.data);
        } else if (envelope.kind == GameSyncEnvelope.Kind.ACTION && isHost) {
            GameAction action = gson.fromJson(envelope.data, GameAction.class);
            applyRemoteAction(action);
        }
    }

    /**
     * Host-side: applies an action relayed from the guest, using the exact
     * same validation/placement code as local input.
     */
    private void applyRemoteAction(GameAction action) {
        if (ctx == null)
            return;

        switch (action.type) {
            case PLACE_ZOMBIE -> {
                if (ctx.getMode() instanceof com.pvz.models.games.modes.capabilities.ZombiePlacer placer) {
                    ZombieCard card = placer.findZombieCard(ctx, action.cardTypeName);
                    if (card != null && placer.isValidPlacement(ctx, action.col, action.lane, card)) {
                        placer.handlePlacement(ctx, action.col, action.lane, card);
                    }
                }
            }
            case PLACE_PLANT -> {
                if (ctx.getMode() instanceof PlantPlacer placer) {
                    PlantCard card = placer.findCard(ctx, action.cardTypeName);
                    if (card != null && placer.isValidPlacement(ctx, action.col, action.lane, card)) {
                        placer.handlePlacement(ctx, action.col, action.lane, card);
                    }
                }
            }
            case COLLECT_SUN -> {
                for (Sun sun : new ArrayList<>(ctx.getSuns())) {
                    if (System.identityHashCode(sun) == action.sunId && !sun.isDone()) {
                        sun.collect(ctx);
                        break;
                    }
                }
            }
        }
    }

    private void sendPlacementAction(GameAction.Type type, String cardTypeName, int col, int lane) {
        GameAction action = GameAction.placeCard(type, cardTypeName, col, lane);
        String inner = gson.toJson(new GameSyncEnvelope(GameSyncEnvelope.Kind.ACTION, gson.toJson(action)));
        NetworkClient.getInstance().sendMatchMessage(matchSession.getMatchId(), inner);
    }

    /**
     * Sends a predefined quick-chat line (text or emoji) to the opponent only.
     * The bubble is shown solely on the receiver's side; the sender sees no
     * feedback popup of its own message.
     */
    private void sendQuickChat(QuickChatMessage.Kind kind, int index) {
        if (matchSession == null)
            return;
        QuickChatMessage msg = new QuickChatMessage(kind, index);
        String inner = gson.toJson(new GameSyncEnvelope(GameSyncEnvelope.Kind.CHAT, gson.toJson(msg)));
        NetworkClient.getInstance().sendMatchMessage(matchSession.getMatchId(), inner);
    }

    /**
     * Sends a sticker (a one-shot PAM clip) to the opponent. The sender's side
     * stays silent; only the receiver plays it, for {@code seconds}.
     */
    private void sendSticker(StickerMessage sticker) {
        if (!isNetworkedMatch || matchSession == null)
            return;
        String inner = gson.toJson(new GameSyncEnvelope(GameSyncEnvelope.Kind.STICKER, gson.toJson(sticker)));
        NetworkClient.getInstance().sendMatchMessage(matchSession.getMatchId(), inner);
    }

    /**
     * Host side: serializes the current rendered picture (the exact FrameConfig
     * list
     * in draw order) into one compact binary {@link RenderFrame}, base64-encodes it
     * (the match relay only carries a String payload), and sends it to the guest.
     */
    private void sendRenderFrame(boolean forceGameOver) {
        RenderFrame rf = new RenderFrame();
        rf.seq = ++snapshotSeq;
        rf.gameOver = forceGameOver || ctx.isGameOver();
        rf.currentSun = ctx.getCurrentSun();
        if (ctx.getMode() instanceof IZombieMode izMode) {
            boolean[] brains = izMode.getBrainsEaten();
            rf.brainCount = brains != null ? brains.length : 0;
            rf.brainsEaten = brains != null ? brains.clone() : new boolean[0];
            rf.plantSurvivalSecondsRemaining = izMode.getPlantSurvivalSecondsRemaining();
        } else {
            rf.brainCount = -1;
            rf.plantSurvivalSecondsRemaining = 0f;
        }
        rf.frames = renderer.collectFrames();

        byte[] payload = rf.write();
        String data = java.util.Base64.getEncoder().encodeToString(payload);
        String inner = gson.toJson(new GameSyncEnvelope(GameSyncEnvelope.Kind.SNAPSHOT, data));
        NetworkClient.getInstance().sendMatchMessage(matchSession.getMatchId(), inner);
    }

    /**
     * Guest side: decode+store the latest rendered frame; it's drawn next render
     * pass.
     */
    private void handleRenderFrame(String base64Data) {
        try {
            byte[] payload = java.util.Base64.getDecoder().decode(base64Data);
            RenderFrame rf = RenderFrame.read(payload);
            if (rf.seq <= lastAppliedSeq) {
                return; // stale / out-of-order
            }
            lastAppliedSeq = rf.seq;
            com.pvz.models.games.modes.variants.IZombieMode iz = null;
            if (ctx != null && ctx.getMode() instanceof com.pvz.models.games.modes.variants.IZombieMode m) {
                iz = m;
            }
            renderer.setRemoteFrame(rf.frames, iz);
            if (ctx != null) {
                ctx.setCurrentSunDirect(rf.currentSun);
                if (rf.gameOver && !ctx.isGameOver()) {
                    ctx.setGameOver(true);
                }
                if (iz != null && rf.brainCount >= 0 && rf.brainsEaten != null) {
                    // The guest uses the synced brain array solely for its HUD overlay.
                    boolean[] local = iz.getBrainsEaten();
                    if (local == null) {
                        local = new boolean[rf.brainCount];
                    }
                    System.arraycopy(rf.brainsEaten, 0, local, 0,
                            Math.min(local.length, rf.brainsEaten.length));
                    iz.setPlantSurvivalSecondsRemaining(rf.plantSurvivalSecondsRemaining);
                }
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            Gdx.app.error("GameController", "Failed to decode remote frame", e);
        }
    }

    public GameRenderer getRenderer() {
        return renderer;
    }

    public Vector3 getTouchPos() {
        return touchPos;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public float getBackgroundYOffset() {
        return backgroundYOffset;
    }

    public PlantCard getPreviewPlantCard() {
        return previewPlantCard;
    }

    public void setPreviewPlantCard(PlantCard card) {
        this.previewPlantCard = card;
    }

    public ZombieCard getPreviewZombieCard() {
        return previewZombieCard;
    }

    public void setPreviewZombieCard(ZombieCard card) {
        this.previewZombieCard = card;
    }

    public void dispose() {
        if (gameUiModal instanceof IZombieOnlineUiModal izombieUi) {
            izombieUi.disposeChat();
        }
        NetworkClient.getInstance().clearPushListener(MessageType.MATCH_MESSAGE);
        NetworkClient.getInstance().clearPushListener(MessageType.OPPONENT_DISCONNECTED);
        NetworkClient.getInstance().clearDisconnectListener();
    }

    private void handleDisconnect() {
        if (ctx == null || ctx.isGameOver())
            return;
        ctx.setGameOver(true);
        paused = true;
        AppContext.getInstance().setMatchSession(null);
        Gdx.app.postRunnable(() -> {
            if (ctx.getMode() instanceof IZombieMode) {
                PvZ2.instance.setScreen(new com.pvz.view.OpponentSelectMenu(PvZ2.instance, seasonName, levelNumber));
            } else {
                PvZ2.instance.setScreen(new GameModesMenu(PvZ2.instance));
            }
        });
    }

    // This method returns the world coordinates of the middle of column
    public static float colToWorldX(int col) {
        return GameMap.START_X + (col * GameMap.TILE_WIDTH) + (GameMap.TILE_WIDTH / 2f);
    }

    public static float laneToWorldY(int lane) {
        return GameMap.TOP_LANE_Y - (lane * GameMap.TILE_HEIGHT) + (GameMap.TILE_HEIGHT / 2f);
    }

    public static int worldXtoCol(float x) {
        return (int) Math.floor((x - GameMap.START_X) / GameMap.TILE_WIDTH);
    }

    public static int worldYtoLane(float y) {
        return (int) Math.ceil((GameMap.TOP_LANE_Y - y) / GameMap.TILE_HEIGHT);
    }

    public static float xToWorldX(float x) {
        return GameMap.START_X + (x * GameMap.TILE_WIDTH);
    }

    public static float yToWorldY(float y) {
        return GameMap.TOP_LANE_Y - (y * GameMap.TILE_HEIGHT);
    }
}
