package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import pvz.skin.PvzSkin;

/**
 * Rey-style two-layer bordered panel: a solid tinted backdrop, an inset padding ring
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
        colorBox.setBackground(skin.newDrawable("white_pixel", bgColor));
        add(colorBox);

        paddingBox = new Table();
        paddingBox.pad(8, 10, 10, 10);
        add(paddingBox);

        contentLayer = new Table();
        contentLayer.setBackground(skin.getDrawable(borderDrawableName));
        contentLayer.pad(17, 22, 24, 22);
        paddingBox.add(contentLayer).grow();
    }
}
