package com.pvz.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class SettingsMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;

    public SettingsMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        Texture bgTexture = game.getGlobalAssetManager().get("textures/backgrounds/MainMenu.png");
        Image bgImage = new Image(bgTexture);
        stage.addActor(bgImage);

        Stack frameStack = new Stack();
        BorderedTable mainPanel = new BorderedTable();
        mainPanel.setSize(550, 650);
        mainPanel.setPosition((1920 - 550) / 2, (1080 - 650) / 2);
        mainPanel.defaults().space(15);
        mainPanel.center();
        frameStack.add(mainPanel);

        // --- Close button overlay (top-right corner of the panel, like News) ----
        ImageButton closeBtn = new ImageButton(skin, "generic_close");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new MainMenu(game));
            }
        });
        Table exitBtnOverlay = new Table();
        exitBtnOverlay.top().right();
        exitBtnOverlay.add(closeBtn).size(48).pad(10);
        frameStack.add(exitBtnOverlay);

        frameStack.setSize(550, 650);
        frameStack.setPosition((1920 - 550) / 2, (1080 - 650) / 2);
        stage.addActor(frameStack);

        Label titleLabel = new Label("Settings", skin, "big");
        titleLabel.setFontScale(1.5f);
        titleLabel.setColor(Color.BLACK);
        mainPanel.add(titleLabel).padBottom(30).row();

        Label difficultyLabel = new Label("Difficulty:", skin);
        difficultyLabel.setFontScale(1.2f);
        difficultyLabel.setColor(Color.BLACK);
        mainPanel.add(difficultyLabel).left().row();

        TextButton[] diffButtons = new TextButton[5];
        Table diffTable = new Table();
        for (int i = 0; i < 5; i++) {
            final int diff = i + 1;
            diffButtons[i] = new TextButton(String.valueOf(diff), skin, "purple");
            diffButtons[i].getLabel().setFontScale(1.3f);
            diffButtons[i].getLabel().setColor(Color.BLACK);
            diffButtons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    for (int j = 0; j < 5; j++) {
                        diffButtons[j].setDisabled(j != diff - 1);
                    }
                }
            });
            diffTable.add(diffButtons[i]).width(60).height(50).pad(5);
        }
        diffButtons[2].setDisabled(false);
        mainPanel.add(diffTable).row();

        Label speedLabel = new Label("Game Speed:", skin);
        speedLabel.setFontScale(1.2f);
        speedLabel.setColor(Color.BLACK);
        mainPanel.add(speedLabel).left().row();

        TextButton[] speedButtons = new TextButton[3];
        Table speedTable = new Table();
        for (int i = 0; i < 3; i++) {
            final int speed = i + 1;
            speedButtons[i] = new TextButton(String.valueOf(speed), skin, "brown");
            speedButtons[i].getLabel().setFontScale(1.3f);
            speedButtons[i].getLabel().setColor(Color.BLACK);
            speedButtons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    for (int j = 0; j < 3; j++) {
                        speedButtons[j].setDisabled(j != speed - 1);
                    }
                }
            });
            speedTable.add(speedButtons[i]).width(60).height(50).pad(5);
        }
        speedButtons[1].setDisabled(false);
        mainPanel.add(speedTable).row();

        CheckBox.CheckBoxStyle gridStyle = new CheckBox.CheckBoxStyle();
        gridStyle.checkboxOn = skin.getDrawable("image_ui_generic_navdot_fill");
        gridStyle.checkboxOff = skin.getDrawable("image_ui_generic_navdot");
        gridStyle.font = skin.getFont("FBUSV8C6EI_3");
        CheckBox showGridCheckBox = new CheckBox("Show Grid", gridStyle);
        showGridCheckBox.getLabel().setFontScale(1.2f);
        showGridCheckBox.getLabel().setColor(Color.BLACK);
        mainPanel.add(showGridCheckBox).left().row();

        CheckBox.CheckBoxStyle debugStyle = new CheckBox.CheckBoxStyle();
        debugStyle.checkboxOn = skin.getDrawable("image_ui_generic_navdot_fill");
        debugStyle.checkboxOff = skin.getDrawable("image_ui_generic_navdot");
        debugStyle.font = skin.getFont("FBUSV8C6EI_3");
        CheckBox debugModeCheckBox = new CheckBox("Debug Mode", debugStyle);
        debugModeCheckBox.getLabel().setFontScale(1.2f);
        debugModeCheckBox.getLabel().setColor(Color.BLACK);
        mainPanel.add(debugModeCheckBox).left().row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}