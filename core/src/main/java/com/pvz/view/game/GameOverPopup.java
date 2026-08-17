package com.pvz.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.PvZ2;

import pvz.skin.PvzSkin;

public class GameOverPopup extends Table {

    private final String titleText;
    private final String exitText;
    private final String retryText;
    private final Runnable exitAction;
    private final Runnable retryAction;

    public GameOverPopup(String titleText,
                         String exitText, Runnable exitAction,
                         String retryText, Runnable retryAction) {
        this.titleText = titleText;
        this.exitText = exitText;
        this.exitAction = exitAction;
        this.retryText = retryText;
        this.retryAction = retryAction;

        setFillParent(true);

        setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 1f)));

        getColor().a = 0f;
        addAction(Actions.fadeIn(2f));
        buildUi();
    }

    private void buildUi() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(
                PvzSkin.get().get("big_outline", Label.LabelStyle.class));
        titleStyle.fontColor = Color.valueOf("39FF14");

        Label titleLbl = new Label(titleText, titleStyle);
        titleLbl.setAlignment(Align.center);
        titleLbl.setFontScale(2.1f);

        TextureRegion brainRegion = PvZ2.textureBank.region("IMAGE_UI_GAMEOVER_FAIL_SCREEN_BRAIN_ONLY");
        Image brainImg = new Image(brainRegion);
        brainImg.setScaling(Scaling.fit);

        Table btnTable = new Table();

        TextButton exitBtn = new TextButton(exitText, PvzSkin.get(), "brown");
        exitBtn.getLabel().setFontScale(0.85f);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (exitAction != null) exitAction.run();
            }
        });

        TextButton retryBtn = new TextButton(retryText, PvzSkin.get(), "purple");
        retryBtn.getLabel().setFontScale(0.85f);
        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (retryAction != null) retryAction.run();
            }
        });

        btnTable.add(exitBtn).size(180, 60).padRight(30);
        btnTable.add(retryBtn).size(180, 60);

        add(titleLbl).padTop(30).padBottom(30).row();
        add(brainImg).size(350, 280).row();
        add(btnTable).expandY().bottom().padBottom(60);
    }
}
