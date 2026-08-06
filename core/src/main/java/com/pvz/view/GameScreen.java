package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.enums.GameAsset;

public class GameScreen extends ScreenAdapter {

    private final AssetManager assetManager;
    private final SpriteBatch batch;

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
    private boolean modalShown = false;

    public GameScreen() {

        batch = new SpriteBatch();

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
        plantSelectModal = new PlantSelectModal(() -> {
            Gdx.app.log("GameScreen", "Selected plants: " + plantSelectModal.getSelectedPlants());
        });
        stage.addActor(plantSelectModal);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        float progress = Math.min(1f, stateTime / transitionDuration);
        float smoothProgress = com.badlogic.gdx.math.Interpolation.fade.apply(progress);

        float currentX = com.badlogic.gdx.math.MathUtils.lerp(startX, endX, smoothProgress);
        camera.position.set(currentX, 720f / 2f, 0);

        if (!modalShown && stateTime >= transitionDuration) {
            modalShown = true;
            plantSelectModal.setVisible(true);
            Gdx.input.setInputProcessor(stage);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        float x = 0;
        float y = 0;

        x-=left.getRegionWidth();
        batch.draw(left, x, y);
        x = 0;

        batch.draw(center, x, y);
        x += center.getRegionWidth();

        batch.draw(right, x, y);

        batch.end();

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
        assetManager.dispose();
        stage.dispose();
    }
}
