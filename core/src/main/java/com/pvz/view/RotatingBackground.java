package com.pvz.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.assets.AssetManager;

public class RotatingBackground {

    private static final float HOLD_TIME = 0f;
    private static final float FADE_TIME = 1.4f;
    private static final String[] BG_PATHS = {
        "textures/backgrounds/bg1.png",
        "textures/backgrounds/bg2.png",
        "textures/backgrounds/bg3.png",
        "textures/backgrounds/bg4.png"
    };

    private RotatingBackground() {}

    public static void install(Group parent, AssetManager assets) {
        Texture[] textures = new Texture[BG_PATHS.length];
        for (int i = 0; i < BG_PATHS.length; i++) {
            textures[i] = assets.get(BG_PATHS[i], Texture.class);
        }

        Image front = new Image(textures[0]);
        front.setFillParent(true);

        Image back = new Image(textures[0]);
        back.setFillParent(true);
        back.getColor().a = 0f;

        parent.addActor(front);
        parent.addActor(back);

        parent.addActor(new Actor() {
            private int currentIndex = 0;
            private float holdTimer = 0;
            private boolean fading = false;

            @Override
            public void act(float delta) {
                super.act(delta);
                if (fading) return;
                holdTimer += delta;
                if (holdTimer >= HOLD_TIME) {
                    holdTimer = 0;
                    fading = true;
                    int next = (currentIndex + 1) % textures.length;

                    back.setDrawable(new TextureRegionDrawable(textures[next]));
                    back.getColor().a = 0f;

                    front.addAction(Actions.fadeOut(FADE_TIME));
                    back.addAction(Actions.sequence(
                        Actions.fadeIn(FADE_TIME),
                        Actions.run(() -> {
                            front.setDrawable(new TextureRegionDrawable(textures[next]));
                            front.getColor().a = 1f;
                            back.getColor().a = 0f;
                            back.clearActions();
                            front.clearActions();
                            currentIndex = next;
                            fading = false;
                        })
                    ));
                }
            }
        });
    }
}
