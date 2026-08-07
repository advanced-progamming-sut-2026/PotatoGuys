package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelLoader;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.user.MyPlant;
import pvz.skin.PvzSkin;

import java.util.ArrayList;

public class GameScreen extends ScreenAdapter {
    public static final int SCREEN_HEIGHT=720;
    public static final int SCREEN_WIDTH=1280;

    private final AssetManager assetManager;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private TextureRegion left;
    private TextureRegion center;
    private TextureRegion right;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    private float stateTime = 0f;
    private final float transitionDuration = 3.0f;
    private float startX;
    private float endX;

    private Stage stage;
    private PlantSelectModal plantSelectModal;
    private GameUiModal gameUiModal;
    private Label readyPlantLabel;

    private GameContext context;

    // این رو به فیلدهای کلاس اضافه کن
    private final Vector3 touchPos = new Vector3();

    private enum State {
        PANNING_FORWARD,
        PLANT_SELECT,
        PANNING_BACK,
        READY_PLANT,
        PLAYING
    }
    private State currentState = State.PANNING_FORWARD;
    private float panBackTime = 0f;
    private final float panBackDuration = 2.5f;
    private float readyPlantTimer = 0f;
    private final float readyPlantDuration = 2.0f;
    private boolean gameStarted = false;

    private final String seasonName;
    private final int levelNumber;

    public GameScreen() {
        this("Ancient Egypt", 1);
    }

    public GameScreen(String seasonName, int levelNumber) {
        this.seasonName = seasonName;
        this.levelNumber = levelNumber;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        assetManager = new AssetManager();
        GameAsset.BACKGROUND_ANCIENT_EGYPT.load(assetManager);
        assetManager.finishLoading();

        TextureAtlas atlas = GameAsset.BACKGROUND_ANCIENT_EGYPT.get(assetManager);

        left = atlas.findRegion("texture_left");
        center = atlas.findRegion("texture");
        right = atlas.findRegion("texture_right");

        if (left == null || center == null || right == null) {
            throw new RuntimeException("Background regions not found in atlas.");
        }

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);

        float wCenter = center.getRegionWidth();
        float wRight = right.getRegionWidth();

        startX = wCenter / 2f;
        endX = wCenter + wRight - 640f;

        camera.position.set(startX, 720f / 2f, 0);
        camera.update();

        // Use a separate FitViewport for UI stage so it stays fixed and centered on screen
        stage = new Stage(new FitViewport(1280, 720));

        // Debug tool & Sun collection / Plant placement click listener
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                float screenX = Gdx.input.getX();
                float screenY = Gdx.input.getY();

                // 1. Check if clicking on suns during PLAYING
                if (currentState == State.PLAYING) {
                    context = AppContext.getInstance().getGameContext();
                    if (context != null) {
                        for (Sun sun : new ArrayList<>(context.getSuns())) {
                            float sunScreenX = 324f + sun.getCol() * 80f + 40f;
                            float sunScreenY = 536f + sun.getLane() * 100f + 50f;
                            float dist = (float) Math.hypot(screenX - sunScreenX, screenY - sunScreenY);
                            if (dist < 60f) {
                                sun.collect(context);
                                Gdx.app.log("GameScreen", "Sun collected! Amount: " + sun.getAmount());
                                return true;
                            }
                        }
                    }
                }

                // 2. Plant placement if card is selected
                if (currentState == State.PLAYING && gameUiModal != null && gameUiModal.getSelectedCard() != null) {
                    if (screenY > 120f) { // below top HUD bar
                        int col = (int) Math.floor((screenX - 324f) / 80f);
                        int lane = (int) Math.floor((screenY - 536f) / 100f);
                        if (col >= 0 && col < 9 && lane >= 0 && lane < 5) {
                            PlantCard card = gameUiModal.getSelectedCard();
                            context = AppContext.getInstance().getGameContext();
                            if (context != null && context.getMode() instanceof PlantPlacer placer) {
                                if (placer.isValidPlacement(context, col, lane, card)) {
                                    placer.handlePlacement(context, col, lane, card);
                                    gameUiModal.setSelectedCard(null);
                                    return true;
                                }
                            }
                        }
                    }
                }

                int col = (int) Math.floor((screenX - 324f) / 80f);
                int lane = (int) Math.floor((screenY - 536f) / 100f);
                System.out.println("[DEBUG Click] Screen X: " + screenX + ", Y: " + screenY +
                                   " | Stage X: " + x + ", Y: " + y + " | Computed Tile: (col=" + col + ", lane=" + lane + ")");
                return false;
            }
        });

        plantSelectModal = new PlantSelectModal(() -> {
            startGameSession();
        });
        stage.addActor(plantSelectModal);

        gameUiModal = new GameUiModal();
        stage.addActor(gameUiModal);

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

    private void startGameSession() {
        try {
            Level level = LevelLoader.loadLevel(seasonName, levelNumber);
            if (level != null) {
                GameEngine.getInstance().reset();
                GameContext context = new GameContext(level);
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
                    context.addCard(new PlantCard(myPlant, sunCost, recharge));
                }
                AppContext.getInstance().setGameContext(context);
                gameUiModal.initCards();

                plantSelectModal.setVisible(false);
                currentState = State.PANNING_BACK;
                panBackTime = 0f;

                Gdx.app.log("GameScreen", "Starting camera pan back for " + seasonName + " Level " + levelNumber);
            } else {
                Gdx.app.error("GameScreen", "Failed to load level: " + seasonName + " Level " + levelNumber);
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error starting game session", e);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        float x = 0;
        float y = 0;

        x -= left.getRegionWidth();
        batch.draw(left, x, y);
        x = 0;

        batch.draw(center, x, y);
        x += center.getRegionWidth();

        batch.draw(right, x, y);

        batch.end();

        switch (currentState) {
            case PANNING_FORWARD:
                stateTime += delta;
                float progressFwd = Math.min(1f, stateTime / transitionDuration);
                float smoothFwd = com.badlogic.gdx.math.Interpolation.fade.apply(progressFwd);
                float currentXFwd = com.badlogic.gdx.math.MathUtils.lerp(startX, endX, smoothFwd);
                camera.position.set(currentXFwd, 720f / 2f, 0);

                if (progressFwd >= 1f) {
                    currentState = State.PLANT_SELECT;
                    plantSelectModal.setVisible(true);
                    Gdx.input.setInputProcessor(stage);
                }
                break;

            case PLANT_SELECT:
                camera.position.set(endX, 720f / 2f, 0);
                break;

            case PANNING_BACK:
                panBackTime += delta;
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
                readyPlantTimer += delta;
                if (readyPlantTimer >= readyPlantDuration) {
                    readyPlantLabel.setVisible(false);
                    currentState = State.PLAYING;
                    gameUiModal.setVisible(true);
                    gameStarted = true;
                }
                break;

            case PLAYING:
                camera.position.set(startX, 720f / 2f, 0);
                gameUiModal.updateHud();
                GameEngine.getInstance().update(delta);
                break;
        }

        // Render tile highlight when in PLAYING state and a plant card is selected
        if (currentState == State.PLAYING && gameUiModal != null && gameUiModal.getSelectedCard() != null) {

            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPos);
            float worldX = touchPos.x;
            float worldY = touchPos.y;

            // گرفتن کاشی زیر موس به راحتی!
            Tile hoveredTile = context.getMap().getTileAt(worldX, worldY);

            if (hoveredTile != null) {
                // رسم هایلایت سبز
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0f, 1f, 0f, 0.4f);

                // دیگه نیازی به محاسبه نیست، خود Tile میدونه کجاست!
                shapeRenderer.rect(hoveredTile.getX(), hoveredTile.getY(),
                    hoveredTile.getWidth(), hoveredTile.getHeight());
                shapeRenderer.end();
            }

            if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
                if (worldY < 600f && hoveredTile != null) {
                    PlantCard card = gameUiModal.getSelectedCard();
                    GameContext context = AppContext.getInstance().getGameContext();

                    if (context != null && context.getMode() instanceof PlantPlacer placer) {
                        // فقط کافیه col و lane رو از آبجکت Tile بگیریم
                        int col = hoveredTile.getCol();
                        int lane = hoveredTile.getLane();

                        if (placer.isValidPlacement(context, col, lane, card)) {
                            placer.handlePlacement(context, col, lane, card);
                            gameUiModal.setSelectedCard(null);
                        }
                    }
                }
            }
        }

        //debug
        if (context!=null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            for (int i = 0; i < context.getMap().getLanes(); i++) {
                shapeRenderer.line(0, GameMap.TOP_LANE_Y - i * GameMap.TILE_HEIGHT, SCREEN_WIDTH, GameMap.TOP_LANE_Y - i * GameMap.TILE_HEIGHT);
            }
            for (int i = 0; i <context.getMap().getColumns(); i++){
                shapeRenderer.line(GameMap.START_X+i*GameMap.TILE_WIDTH,0,GameMap.START_X+i*GameMap.TILE_WIDTH,SCREEN_HEIGHT);
            }
            shapeRenderer.end();
        }
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        assetManager.dispose();
        stage.dispose();
    }
}
