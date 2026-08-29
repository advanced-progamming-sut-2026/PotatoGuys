package com.pvz.view.game.ui;

import java.util.List;
import java.util.function.BiConsumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.network.game.QuickChatMessage;
import com.pvz.view.MenuUiKit;

import pvz.skin.PvzSkin;

/**
 * Bottom-right quick-chat panel for online I,Zombie matches.
 *
 * <p>
 * It offers 3 canned messages (drawn with the same background the login /
 * register text boxes use) and 3 emojis loaded from {@code assets/images/emojies}.
 * Pressing one sends a {@link QuickChatMessage} to the opponent through the
 * existing MATCH_MESSAGE relay. The bubble pops up at the bottom-right only on
 * the opponent's side, so the sender never sees its own sent emoji/text.
 *
 * <p>
 * Layout is tuned for the 1280x720 gameplay viewport: the bar hugs the
 * bottom-right corner (just left of the shovel button) and incoming/outgoing
 * bubbles stack upward above it, fading out after a short while. Emojis load at
 * their natural size but are drawn with {@code Scaling.fit} so the big 640px
 * PNGs are scaled to fit the small on-screen slots/bubbles.
 */
public class QuickChatUi extends Table {

    private static final String[] MESSAGES = {
            "Good luck!",
            "Well done!",
            "Nice move!"
    };

    private static final String[] EMOJI_PATHS = {
            "images/emojies/Tears of Joy Emoji.png",
            "images/emojies/Voltage Emoji.png",
            "images/emojies/Loudly Crying Face Emoji.png"
    };

    /** Seconds a chat bubble stays visible before it fades out. */
    private static final float BUBBLE_LIFETIME = 2.6f;
    /** How many bubbles are kept on screen at once (oldest drops first). */
    private static final int MAX_BUBBLES = 4;

    /** Right padding so the bar sits left of the shovel button (58px + margin). */
    private static final float BAR_PAD_RIGHT = 92f;
    /** Bottom padding for the bar itself. */
    private static final float BAR_PAD_BOTTOM = 15f;

    private final Skin skin;
    private final BiConsumer<QuickChatMessage.Kind, Integer> onSend;
    private final Texture[] emojiTextures = new Texture[EMOJI_PATHS.length];
    private final Table popupLayer = new Table();
    private final List<Texture> ownedTextures = new java.util.ArrayList<>();

    public QuickChatUi(BiConsumer<QuickChatMessage.Kind, Integer> onSend) {
        this.onSend = onSend;
        this.skin = PvzSkin.get();

        setFillParent(true);
        bottom().right();

        for (int i = 0; i < EMOJI_PATHS.length; i++) {
            emojiTextures[i] = loadEmojiTexture(EMOJI_PATHS[i]);
        }

        // Chat-bubble layer, pinned just above the bar.
        popupLayer.setFillParent(true);
        popupLayer.bottom().right();
        popupLayer.pad(0f, 0f, BAR_PAD_BOTTOM + 96f, BAR_PAD_RIGHT);
        addActor(popupLayer);

        Table msgRow = new Table();
        for (int i = 0; i < MESSAGES.length; i++) {
            msgRow.add(buildMessageButton(MESSAGES[i], i)).padRight(5f);
        }

        Table emojiRow = new Table();
        for (int i = 0; i < EMOJI_PATHS.length; i++) {
            emojiRow.add(buildEmojiButton(i)).size(48f, 46f).padRight(5f);
        }

        Table bar = new Table();
        bar.bottom().right();
        bar.add(msgRow).row();
        bar.add(emojiRow).padTop(6f);

        add(bar).padBottom(BAR_PAD_BOTTOM).padRight(BAR_PAD_RIGHT);

        // Keep chat bubbles above the bar in case the two ever overlap.
        popupLayer.toFront();
    }

    /**
     * Pops a chat bubble up at the bottom-right. Called for received messages
     * so the opponent sees each item arrive (never for the sender's own items).
     */
    public void showChat(QuickChatMessage msg) {
        if (msg == null)
            return;

        Actor bubble;
        if (msg.kind == QuickChatMessage.Kind.EMOJI) {
            if (msg.index < 0 || msg.index >= EMOJI_PATHS.length)
                return;
            bubble = buildEmojiBubble(msg.index);
        } else {
            if (msg.index < 0 || msg.index >= MESSAGES.length)
                return;
            bubble = buildMessageBubble(MESSAGES[msg.index]);
        }

        if (popupLayer.getChildren().size >= MAX_BUBBLES) {
            popupLayer.getChildren().get(0).remove();
        }
        popupLayer.add(bubble).row();

        bubble.setColor(1f, 1f, 1f, 0f);
        bubble.addAction(Actions.sequence(
                Actions.fadeIn(0.2f),
                Actions.delay(BUBBLE_LIFETIME),
                Actions.fadeOut(0.5f),
                Actions.removeActor()));
    }

    public void dispose() {
        for (Texture t : ownedTextures) {
            t.dispose();
        }
        ownedTextures.clear();
    }

    /**
     * One canned message as a small button that looks like the game's text
     * boxes (same background drawable + "big" font), so the chat stays visually
     * consistent with the login/register fields. Clicking it both sends the
     * message and stops the tap from reaching the gameplay input handler below.
     */
    private Actor buildMessageButton(String text, int index) {
        Drawable bg = textBoxBackground();
        Drawable over = textBoxFocusedBackground();

        Table slot = new Table();
        slot.setBackground(bg);
        slot.pad(3f, 8f, 3f, 8f);

        Label label = new Label(text, skin, "big");
        label.setFontScale(0.38f);
        label.setColor(Color.BLACK);
        slot.add(label);

        slot.setTouchable(Touchable.enabled);
        slot.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean handled = super.touchDown(event, x, y, pointer, button);
                event.stop();
                return handled;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    slot.setBackground(over);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    slot.setBackground(bg);
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSend.accept(QuickChatMessage.Kind.TEXT, index);
            }
        });
        return slot;
    }

    private Actor buildEmojiButton(int index) {
        Image img = new Image(new TextureRegionDrawable(emojiTextures[index]));
        img.setScaling(Scaling.fit);

        Table slot = new Table();
        slot.setBackground(skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0f)));
        slot.add(img).size(42f, 42f);

        slot.setTouchable(Touchable.enabled);
        slot.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean handled = super.touchDown(event, x, y, pointer, button);
                event.stop();
                return handled;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSend.accept(QuickChatMessage.Kind.EMOJI, index);
            }
        });
        return slot;
    }

    /** Message bubble shown for incoming/outgoing chat (textbox look, slightly larger). */
    private Actor buildMessageBubble(String text) {
        Table bubble = new Table();
        bubble.setBackground(textBoxBackground());
        bubble.pad(5f, 12f, 5f, 12f);

        Label label = new Label(text, skin, "big");
        label.setFontScale(0.52f);
        label.setColor(Color.BLACK);
        bubble.add(label);

        bubble.setTouchable(Touchable.disabled);
        return bubble;
    }

    /** Emoji bubble: the 640px PNG scaled with {@code Scaling.fit} into a small box. */
    private Actor buildEmojiBubble(int index) {
        Image img = new Image(new TextureRegionDrawable(emojiTextures[index]));
        img.setScaling(Scaling.fit);

        Table bubble = new Table();
        bubble.setBackground(skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0f)));
        bubble.pad(6f);
        bubble.add(img).size(94f, 94f);

        bubble.setTouchable(Touchable.disabled);
        return bubble;
    }

    private Drawable textBoxBackground() {
        TextField.TextFieldStyle tfs = skin.get(TextField.TextFieldStyle.class);
        if (tfs != null && tfs.background != null) {
            return tfs.background;
        }
        return skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0f));
    }

    private Drawable textBoxFocusedBackground() {
        TextField.TextFieldStyle tfs = skin.get(TextField.TextFieldStyle.class);
        if (tfs != null && tfs.focusedBackground != null) {
            return tfs.focusedBackground;
        }
        return textBoxBackground();
    }

    private Texture loadEmojiTexture(String path) {
        if (Gdx.files.internal(path).exists()) {
            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            ownedTextures.add(t);
            return t;
        }
        Texture fallback = MenuUiKit.solidTexture(new Color(0.95f, 0.75f, 0.2f, 1f));
        ownedTextures.add(fallback);
        return fallback;
    }
}