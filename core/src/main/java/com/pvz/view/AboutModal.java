package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class AboutModal extends Table {

    private static final float MODAL_WIDTH = 760f;
    private static final float MODAL_HEIGHT = 580f;

    private static final Color NAME_COLOR = Color.BLACK;
    private static final Color CREDIT_COLOR = new Color(0.45f, 0.45f, 0.45f, 1f);

    private static final String TEAM_NAME = "Potato Guys";
    private static final String[] TEAM_NAMES = { "Mahdi Shakeri", "Yousof_RD", "SedHHN" };

    public AboutModal() {
        super();
        center();
        setVisible(false);

        Skin skin = PvzSkin.get();

        BorderedTable content = new BorderedTable();
        content.top();
        content.pad(30, 40, 40, 40);

        // --- Header bar ----------------------------------------------------
        Table headerBar = new Table();
        headerBar.setBackground(skin.getDrawable("image_ui_mainmenu_mm_settings_tab_10"));
        headerBar.pad(16, 40, 16, 40);

        Label teamName = new Label(TEAM_NAME, skin, "big");
        teamName.setFontScale(1.6f);
        teamName.setColor(NAME_COLOR);

        Table titleCol = new Table();
        titleCol.add(teamName).left();

        headerBar.add(titleCol).left();

        content.add(headerBar).width(660).row();

        // --- Subtitle at the bottom of the team section --------------------
        Label subtitle = new Label("Meet the team behind the game", skin, "medium");
        subtitle.setFontScale(0.95f);
        subtitle.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        content.add(subtitle).width(560).left().padTop(24).row();

        // --- Team members ---------------------------------------------------
        Table members = new Table();
        members.padTop(30);

        for (String name : TEAM_NAMES) {
            Table rowPanel = new Table();
            rowPanel.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
            rowPanel.pad(14, 30, 14, 30);

            Label nameLabel = new Label(name, skin, "medium");
            nameLabel.setFontScale(1.25f);
            nameLabel.setColor(NAME_COLOR);

            rowPanel.add(nameLabel).left();

            members.add(rowPanel).width(560).left().padBottom(14).row();
        }
        content.add(members).row();

        // --- Credit footer ---------------------------------------------------
        Label credit = new Label("Original game by Pop Cap studio", skin, "medium");
        credit.setFontScale(0.9f);
        credit.setColor(CREDIT_COLOR);
        content.add(credit).padTop(30);

        // --- Close button overlay -------------------------------------------
        ImageButton exitBtn = new ImageButton(skin, "generic_close_circle");
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                setVisible(false);
            }
        });

        Stack frameStack = new Stack();
        frameStack.add(content);

        Table exitBtnOverlay = new Table();
        exitBtnOverlay.top().right();
        exitBtnOverlay.add(exitBtn).size(48).pad(10);
        frameStack.add(exitBtnOverlay);

        add(frameStack).size(MODAL_WIDTH, MODAL_HEIGHT);
    }
}