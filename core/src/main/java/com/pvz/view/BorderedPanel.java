package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import pvz.skin.PvzSkin;

/**
 * Two-layer bordered panel: a solid tinted backdrop, an inset padding ring
 * and a bordered content layer on top. Add your widgets to {@link #contentLayer}.
 */
public class BorderedPanel extends Stack {

    public final Table colorBox;
    public final Table paddingBox;
    public final Table contentLayer;

    public BorderedPanel(Color bgColor) {
        this(bgColor, "image_ui_dialog_asset_dialogborder_10");
    }

    public BorderedPanel(Color bgColor, String borderDrawableName) {
        Skin skin = PvzSkin.get();

        colorBox = new Table();
        colorBox.setBackground(skin.newDrawable("image_ui_dialog_asset_inner_bkgd_10", bgColor));

        Table bgWrap = new Table();
        bgWrap.pad(25, 32, 34, 32);
        bgWrap.add(colorBox).grow();
        add(bgWrap);

        paddingBox = new Table();
        paddingBox.pad(8, 10, 10, 10);
        add(paddingBox);

        contentLayer = new Table();
        contentLayer.setBackground(skin.getDrawable(borderDrawableName));
        contentLayer.pad(17, 22, 24, 22);
        paddingBox.add(contentLayer).grow();
    }
}
