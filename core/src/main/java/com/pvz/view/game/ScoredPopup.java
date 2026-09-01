package com.pvz.view.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import pvz.skin.PvzSkin;

/**
 * A short-lived floating notification shown when a scoring pattern fires in
 * the scored ("mini-point") game mode. It fades in, holds briefly, fades out
 * and removes itself from the stage.
 */
public class ScoredPopup extends Table {

    private static final float FADE = 0.25f;
    private static final float HOLD = 1.3f;

    public ScoredPopup(int points) {
        setFillParent(true);

        Label.LabelStyle base = PvzSkin.get().get("medium_outline", Label.LabelStyle.class);
        Label.LabelStyle style = new Label.LabelStyle(base.font, Color.valueOf("FFD54F"));

        Label lbl = new Label("You scored " + points + " points!", style);
        lbl.setFontScale(1.4f);

        add(lbl).pad(8).expandX().top().padTop(140);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

        getColor().a = 0f;
        addAction(Actions.sequence(
                Actions.fadeIn(FADE),
                Actions.delay(HOLD),
                Actions.fadeOut(FADE),
                Actions.removeActor()));
    }
}
