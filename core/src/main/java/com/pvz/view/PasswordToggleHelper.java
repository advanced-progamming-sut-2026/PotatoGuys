package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public final class PasswordToggleHelper {

    private static final float TOGGLE_SIZE = 28f;

    private PasswordToggleHelper() {}

    /**
     * Creates a {@link Table} containing a checkbox toggle and a "Show password"
     * label that switches a password {@link TextField} between masked and visible modes.
     *
     * @return a Table the caller should add to their layout
     */
    public static Table createToggle(TextField passwordField, Skin skin) {
        Texture enabledTex  = new Texture(Gdx.files.internal("images/checkbox_enabled.png"));
        Texture disabledTex = new Texture(Gdx.files.internal("images/checkbox_disabled.png"));

        TextureRegionDrawable enabledDrawable  = new TextureRegionDrawable(enabledTex);
        TextureRegionDrawable disabledDrawable = new TextureRegionDrawable(disabledTex);

        ImageButton toggle = new ImageButton(disabledDrawable);
        toggle.getStyle().imageChecked = enabledDrawable;
        toggle.getStyle().imageCheckedOver = enabledDrawable;
        toggle.getImage().setScaling(com.badlogic.gdx.utils.Scaling.fit);
        toggle.getImageCell().size(TOGGLE_SIZE, TOGGLE_SIZE);

        toggle.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                passwordField.setPasswordMode(!toggle.isChecked());
                passwordField.setPasswordCharacter('*');
            }
        });

        Label showLabel = new Label("Show password", skin);
        showLabel.setColor(Color.LIGHT_GRAY);
        showLabel.setFontScale(1.4f);

        Table wrapper = new Table();
        wrapper.add(showLabel).padRight(6f);
        wrapper.add(toggle).size(TOGGLE_SIZE);

        return wrapper;
    }
}
