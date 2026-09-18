package com.pvz.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import com.pvz.PvZ2;
import com.pvz.view.BorderedPanel;
import com.pvz.view.MenuUiKit;

import pvz.skin.PvzSkin;

public class GameWinPopup extends Table {

    private final String titleText;
    private final String messageText;
    private final String exitText;
    private final String nextText;
    private final Runnable exitAction;
    private final Runnable nextAction;
    private final int rewardCoins;
    private final int rewardDiamonds;

    public GameWinPopup(String titleText, String messageText,
                        String exitText, Runnable exitAction,
                        String nextText, Runnable nextAction) {
        this(titleText, messageText, exitText, exitAction, nextText, nextAction, 0, 0);
    }

    public GameWinPopup(String titleText, String messageText,
                        String exitText, Runnable exitAction,
                        String nextText, Runnable nextAction,
                        int rewardCoins, int rewardDiamonds) {
        this.titleText = titleText;
        this.messageText = messageText;
        this.exitText = exitText;
        this.exitAction = exitAction;
        this.nextText = nextText;
        this.nextAction = nextAction;
        this.rewardCoins = rewardCoins;
        this.rewardDiamonds = rewardDiamonds;

        setFillParent(true);

        setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.85f)));

        getColor().a = 0f;
        addAction(Actions.fadeIn(1.5f));
        buildUi();
    }

    private void buildUi() {
        BorderedPanel boardPanel = new BorderedPanel(Color.valueOf("F5DEB3"));
        Table content = boardPanel.contentLayer;
        content.pad(25f, 30f, 30f, 30f);

        Label.LabelStyle textStyle = PvzSkin.get().get("medium_outline", Label.LabelStyle.class);

        Label titleLbl = new Label(titleText, textStyle);
        titleLbl.setColor(Color.valueOf("FF8C00"));
        titleLbl.setFontScale(2.0f);
        titleLbl.setAlignment(Align.center);

        Image separatorLine = new Image(PvzSkin.get().newDrawable("white_pixel", Color.valueOf("D2B48C")));

        Label msgLbl = new Label(messageText, textStyle);
        msgLbl.setColor(Color.valueOf("556B2F"));
        msgLbl.setFontScale(1.2f);
        msgLbl.setAlignment(Align.center);

        Table btnTable = new Table();

        TextButton exitBtn = new TextButton(exitText, PvzSkin.get(), "brown");
        exitBtn.getLabel().setFontScale(0.85f);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (exitAction != null) exitAction.run();
            }
        });

        btnTable.add(exitBtn).size(180, 55);

        if (nextText != null) {
            TextButton nextBtn = new TextButton(nextText, PvzSkin.get(), "purple");
            nextBtn.getLabel().setFontScale(0.85f);
            nextBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (nextAction != null) nextAction.run();
                }
            });
            btnTable.add(nextBtn).size(180, 55).padLeft(30);
        }

        content.add(titleLbl).padBottom(15f).row();
        content.add(separatorLine).growX().height(3f).padBottom(20f).row();
        content.add(msgLbl).width(650f).height(100f).center().row();

        if (rewardCoins > 0 || rewardDiamonds > 0) {
            Table rewardRow = new Table();
            rewardRow.center();

            if (rewardCoins > 0) {
                rewardRow.add(MenuUiKit.resourceWidget(PvzSkin.get(), "textures/ui/coin_icon.png",
                    Color.valueOf("FFD700"), "+" + rewardCoins, 180, 58)).padRight(15);
            }
            if (rewardDiamonds > 0) {
                rewardRow.add(MenuUiKit.resourceWidget(PvzSkin.get(), "textures/ui/diamond_icon.png",
                    Color.valueOf("87CEFA"), "+" + rewardDiamonds, 180, 58));
            }

            content.add(rewardRow).padBottom(15).row();
        }

        content.add(btnTable).padBottom(10);

        boardPanel.pack();
        add(boardPanel).center();
    }
}
