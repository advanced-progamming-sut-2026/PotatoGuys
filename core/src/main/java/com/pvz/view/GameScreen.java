package com.pvz.view;

import java.util.ArrayList;

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
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.engine.GameEngine;
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
import com.pvz.models.user.MyPlant;

import pvz.skin.PvzSkin;

public class GameScreen extends ScreenAdapter {
    public static final int SCREEN_HEIGHT = 720;
    public static final int SCREEN_WIDTH = 1280;

    GameController controller;

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

    private final Vector3 touchPos = new Vector3();

    private boolean checkSunClick(float worldX, float worldY) {
        context = AppContext.getInstance().getGameContext();
        if (context != null) {
            for (Sun sun : new ArrayList<>(context.getSuns())) {
                if (sun.isDone()) continue;
                float sunX = sun.getX();
                float sunY = sun.getY();
                float dist = (float) Math.hypot(worldX - sunX, worldY - sunY);
                if (dist < 50f) {
                    sun.collect(context);
                    Gdx.app.log("GameScreen", "Sun collected! Amount: " + sun.getAmount());
                    return true;
                }
            }
        }
        return false;
    }

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

    public GameScreen(String seasonName, int levelNumber) {
        this.seasonName = seasonName;
        this.levelNumber = levelNumber;

        batch = PvZ2.batch;
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

        stage = new Stage(new FitViewport(1280, 720));

        // مدیریت کامل رویدادهای لمس و کلیک به‌صورت یکپارچه در Stage
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (currentState == State.PLAYING) {
                    context = AppContext.getInstance().getGameContext();
                    context = AppContext.getInstance().getGameContext();
                    if (context != null) {
                        // تبدیل ورودی ماوس/لمس به مختصات دقیق World
                        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(touchPos);

                        // ۱. ابتدا کلیک روی خورشید بررسی می‌شود
                        if (checkSunClick(touchPos.x, touchPos.y)) {
                            if (gameUiModal != null) {
                                gameUiModal.setSelectedCard(null);
                            }
                            return true;
                        }

                        // ۲. کاشت گیاه در صورت انتخاب کارت
                        if (gameUiModal != null && gameUiModal.getSelectedCard() != null) {
                            Tile hoveredTile = context.getMap().getTileAt(touchPos.x, touchPos.y);
                            if (hoveredTile != null) {
                                PlantCard card = gameUiModal.getSelectedCard();
                                if (context.getMode() instanceof PlantPlacer placer) {
                                    int col = hoveredTile.getCol();
                                    int lane = hoveredTile.getLane();

                                    if (placer.isValidPlacement(context, col, lane, card)) {
                                        placer.handlePlacement(context, col, lane, card);
                                        gameUiModal.setSelectedCard(null);
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

        plantSelectModal = new PlantSelectModal(this::startGameSession);
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
                context = newContext;
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

        // ۱. منطق وضعیت‌ها و موقعیت دوربین
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
                break;
        }

        // Check sun clicks in PLAYING state on left click
        if (currentState == State.PLAYING && Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPos);
            if (checkSunClick(touchPos.x, touchPos.y)) {
                if (gameUiModal != null) {
                    gameUiModal.setSelectedCard(null);
                }
            }
        }

        // ۲. به روزرسانی دوربین و ماتریس‌ها
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // ۳. رسم background
        batch.begin();
        float x = -left.getRegionWidth();
        batch.draw(left, x, 0);
        batch.draw(center, 0, 0);
        batch.draw(right, center.getRegionWidth(), 0);
        batch.end();

        context = AppContext.getInstance().getGameContext();

        // ۴. آپدیت Engine
        if (currentState == State.PLAYING) {
            GameEngine.getInstance().update(delta);
        }

        // ۵. رسم هایلایت خانه زیر ماوس (در صورت انتخاب کارت)
        if (currentState == State.PLAYING && gameUiModal != null && gameUiModal.getSelectedCard() != null && context != null) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPos);

            Tile hoveredTile = context.getMap().getTileAt(touchPos.x, touchPos.y);

            if (hoveredTile != null) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0f, 1f, 0f, 0.4f);
                shapeRenderer.rect(hoveredTile.getX(), hoveredTile.getY(), hoveredTile.getWidth(), hoveredTile.getHeight());
                shapeRenderer.end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }
        }

        // // ۶. رسم خورشیدها و خطوط دیباگ گرید
        // if (context != null) {
        //     shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //     // رسم خورشیدها
        //     shapeRenderer.setColor(Color.YELLOW);
        //     for (Sun sun : new ArrayList<>(context.getSuns())) {
        //         if (!sun.isDone()) {
        //             shapeRenderer.circle(sun.getX(), sun.getY(), 50);
        //         }
        //     }

        //     // رسم خطوط گرید دیباگ
        //     shapeRenderer.setColor(Color.RED);
        //     for (int i = 0; i < context.getMap().getLanes(); i++) {
        //         float gridY = GameMap.TOP_LANE_Y - i * GameMap.TILE_HEIGHT;
        //         shapeRenderer.rect(0, gridY - 0.5f, SCREEN_WIDTH, 1);
        //     }
        //     for (int i = 0; i < context.getMap().getColumns(); i++) {
        //         float gridX = GameMap.START_X + i * GameMap.TILE_WIDTH;
        //         shapeRenderer.rect(gridX - 0.5f, 0, 1, SCREEN_HEIGHT);
        //     }

        //     for(Plant a : context.getPlants()){
        //         shapeRenderer.circle(GameController.xToWorldX(a.getCol()), GameController.yToWorldY(a.getLane()), 10);
        //     }

        //     for(Projectile a : context.getProjectiles()){
        //         shapeRenderer.circle(GameController.xToWorldX(a.getX()), GameController.yToWorldY(a.getY()), 10);
        //     }

        //     for(Zombie a : context.getZombies()){
        //         shapeRenderer.circle(GameController.xToWorldX(a.getX()), GameController.yToWorldY(a.getLane()), 10);
        //     }

        //     shapeRenderer.end();
        // }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
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