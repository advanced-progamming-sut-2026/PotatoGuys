package com.pvz.view.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.PvZ2;
import com.pvz.view.BorderedPanel;

import pvz.skin.PvzSkin;

/**
 * Pause dialog: the PvZ2 pause menu window with the decorative
 * topper banner + sunflower, "Game Paused" title, music/sound sliders and the
 * SAVE AND EXIT / RESTART / RESUME buttons. Layout is a verbatim port of the
 * PauseMenuPopup from the phase-0 group project, adapted to this codebase's
 * static skin/texture accessors.
 */
public class PauseMenuPopup extends BorderedPanel {

    public PauseMenuPopup(Runnable onSaveAndQuit, Runnable onRestart, Runnable onResume) {
        super(Color.valueOf("8F4909"));

        TextureRegion topperRegion = PvZ2.textureBank.region("IMAGE_UI_PAUSEMENU_WINDOWTOPPER");
        TextureRegion sunflowerRegion = PvZ2.textureBank.region("IMAGE_UI_PAUSEMENU_SUNFLOWER_TOPPER");
        TextureRegion sliderKnob = PvZ2.textureBank.region("IMAGE_UI_PAUSEMENU_SLIDER_BOLT");

        Stack topDecoration = new Stack();
        Image topperImage = new Image(topperRegion);
        Image sunflowerImage = new Image(sunflowerRegion);

        topperImage.setScaling(Scaling.none);
        sunflowerImage.setScaling(Scaling.none);

        Container<Image> sunflowerContainer = new Container<>(sunflowerImage);
        sunflowerContainer.align(Align.top | Align.center);

        sunflowerContainer.padTop(-25f);

        topDecoration.add(topperImage);
        topDecoration.add(sunflowerContainer);

        Label titleLabel = new Label("Game Paused", PvzSkin.get().get("big_outline", Label.LabelStyle.class));
        titleLabel.setAlignment(Align.center);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle(PvzSkin.get().get("default-horizontal", Slider.SliderStyle.class));
        sliderStyle.knob = new TextureRegionDrawable(sliderKnob);

        Table slidersTable = new Table();
        Label musicLabel = new Label("Music", PvzSkin.get().get("medium_outline", Label.LabelStyle.class));
        Slider musicSlider = new Slider(0, 100, 1, false, sliderStyle);
        slidersTable.add(musicLabel).padRight(15).align(Align.right);
        slidersTable.add(musicSlider).width(200).row();

        Label sfxLabel = new Label("Sound FX", PvzSkin.get().get("medium_outline", Label.LabelStyle.class));
        Slider sfxSlider = new Slider(0, 100, 1, false, sliderStyle);
        slidersTable.add(sfxLabel).padRight(15).align(Align.right).padTop(10);
        slidersTable.add(sfxSlider).width(200).padTop(10).row();

        TextButton exitButton = new TextButton("SAVE AND EXIT", PvzSkin.get().get("purple", TextButton.TextButtonStyle.class));
        TextButton restartButton = new TextButton("RESTART", PvzSkin.get(), "purple");
        TextButton resumeButton = new TextButton("RESUME", PvzSkin.get(), "brown");

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onSaveAndQuit != null) {
                    onSaveAndQuit.run();
                }
            }
        });

        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onRestart != null) {
                    onRestart.run();
                }
            }
        });

        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onResume != null) {
                    onResume.run();
                }
            }
        });

        Table buttonsTable = new Table();
        buttonsTable.add(exitButton).padRight(10);
        buttonsTable.add(restartButton).padRight(10);
        buttonsTable.add(resumeButton);

        this.contentLayer.add(topDecoration).align(Align.center).padTop(-50).row();
        this.contentLayer.add(titleLabel).padTop(-400).row();
        this.contentLayer.add(slidersTable).padBottom(30).row();
        this.contentLayer.add(buttonsTable).align(Align.bottom).padBottom(-100);

        this.pack();
    }
}
