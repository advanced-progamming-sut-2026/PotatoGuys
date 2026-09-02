package com.pvz.controller.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import pvz.skin.PvzSkin;

public class NotificationSystem {
    private static final float DURATION = 3.5f;

    private final Label notificationLabel;
    private final Table notificationTable;

    public NotificationSystem(Stage stage) {
        notificationLabel = new Label("", PvzSkin.get(), "big");
        notificationLabel.setColor(Color.RED);
        notificationLabel.setFontScale(1.3f);
        notificationLabel.setAlignment(Align.center);

        notificationTable = new Table();
        notificationTable.setFillParent(true);
        notificationTable.center();
        notificationTable.add(notificationLabel).padTop(-80);
        notificationTable.setVisible(false);
        stage.addActor(notificationTable);
    }

    public void show(String text) {
        notificationLabel.setText(text);
        notificationTable.setVisible(true);
        notificationTable.clearActions();
        notificationTable.addAction(
                Actions.sequence(
                        Actions.delay(DURATION),
                        Actions.run(() -> notificationTable.setVisible(false))));
        notificationTable.toFront();
    }
}
