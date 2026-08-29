package com.pvz.view.game;

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.view.MenuUiKit;

import com.pvz.PvZ2;
import com.pvz.network.game.Reaction;

import pvz.skin.PvzSkin;

/**
 * Small bottom-right quick-reaction picker shown during a networked I,Zombie
 * match. Top row: five preset texts. Bottom row: three emoji faces. Any click
 * reports a {@link Reaction} through {@code onReaction} and the modal closes
 * itself.
 *
 * <p>
 * The panel keeps its own touchable area tiny (only the visible box), so
 * everything outside it still falls through to gameplay — the player can keep
 * placing plants/zombies while it's open.
 */
public class ReactionUiModal extends Table {

    private static final String[] TEXT_REACTIONS =
            { "Hello!", "Looser", "Nice", "Not Bad", "Bye" };

    private static final String[] EMOJI_REACTIONS = {
            "IMAGE_UI_JOUST_AVATARS_AVATAR_17",
            "IMAGE_UI_JOUST_AVATARS_AVATAR_13",
            "IMAGE_UI_JOUST_AVATARS_AVATAR_3"
    };

    private final Consumer<Reaction> onReaction;

    public ReactionUiModal(Consumer<Reaction> onReaction) {
        this.onReaction = onReaction;
        setTouchable(Touchable.enabled);
        setVisible(false);

        Table border = new Table();
        border.setTouchable(Touchable.enabled);
        border.setBackground(PvzSkin.get().newDrawable("white_pixel", Color.valueOf("8F4909")));

        Table body = new Table();
        body.setTouchable(Touchable.enabled);
        body.setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0.05f, 0.05f, 0.08f, 0.85f)));
        body.pad(8f);

        Table textRow = new Table();
        for (String text : TEXT_REACTIONS) {
            textRow.add(makeTextButton(text)).pad(2f);
        }
        body.add(textRow).row();

        Table emojiRow = new Table();
        for (String atlasId : EMOJI_REACTIONS) {
            emojiRow.add(makeEmojiButton(atlasId)).size(46f, 46f).pad(3f);
        }
        body.add(emojiRow).padTop(5f);

        border.add(body).pad(2f);
        add(border).pad(2f);
        pack();
    }

    private Table makeTextButton(String text) {
        Table btn = new Table();
        btn.setTouchable(Touchable.enabled);
        btn.setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0.22f, 0.22f, 0.28f, 0.95f)));
        Label label = new Label(text, PvzSkin.get(), "medium");
        label.setColor(Color.WHITE);
        label.setFontScale(0.72f);
        label.setAlignment(Align.center);
        btn.add(label).pad(4f, 9f, 4f, 9f);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onReaction != null) {
                    onReaction.accept(new Reaction(Reaction.Kind.TEXT, text));
                }
            }
        });
        return btn;
    }

    private ImageButton makeEmojiButton(String atlasId) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        Drawable fallback = new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0.35f, 0.35f, 0.35f, 0.95f)));
        style.imageUp = PvZ2.textureBank.region(atlasId) != null
                ? new TextureRegionDrawable(PvZ2.textureBank.region(atlasId))
                : fallback;
        style.imageDown = fallback;
        style.imageOver = fallback;

        ImageButton btn = new ImageButton(style);
        Image img = btn.getImage();
        img.setScaling(Scaling.fit);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onReaction != null) {
                    onReaction.accept(new Reaction(Reaction.Kind.EMOJI, atlasId));
                }
            }
        });
        return btn;
    }
}