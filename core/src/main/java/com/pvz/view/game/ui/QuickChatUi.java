package com.pvz.view.game.ui;

import java.util.List;
import java.util.function.BiConsumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.PvZ2;
import com.pvz.network.game.QuickChatMessage;
import com.pvz.view.MenuUiKit;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Quick-chat for online I,Zombie matches, merging the reaction picker's
 * interaction model with the canned text/emoji payloads:
 *
 * <p>
 * A small toggle button pinned just above the shovel opens a compact picker
 * window where the player chooses one of the preset messages (drawn with the
 * game's text-box background) or one of the emojis (loaded from
 * {@code assets/images/emojies}). Selecting one sends a {@link QuickChatMessage}
 * to the opponent through the existing MATCH_MESSAGE relay and closes the
 * picker.
 *
 * <p>
 * The sender never sees its own item; the opponent renders it as a bubble
 * popping up at the bottom-right, above the toggle button (text-box bubble for
 * messages, the emoji image for emojis), fading out after a short while. All
 * coordinates are tuned for the 1280x720 gameplay viewport.
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

    /** Size and corner padding of the toggle button (sits above the shovel). */
    private static final float TOGGLE_SIZE = 50f;
    private static final float TOGGLE_PAD_BOTTOM = 82f;
    private static final float TOGGLE_PAD_RIGHT = 20f;
    /** Right edge margin of the picker window and its offset above the button. */
    private static final float PICKER_MARGIN = 20f;
    private static final float PICKER_CLEARANCE = 6f;
    /**
     * Extra vertical space between the bottom of the first chat bubble and the
     * top of the topmost panel button, so incoming bubbles don't sit flush on
     * it. Bubble layer is parked above both the chat toggle (82 + 50 = 132)
     * and the sticker button above it (140 + 50 = 190).
     */
    private static final float CHAT_STACK_TOP = 190f;
    private static final float FIRST_BUBBLE_CLEARANCE = 20f;

    /**
     * Shared tuning for the pop-up items (stickers, chat bubbles, emoji
     * bubbles): shift everything 10px left and 120px down. Keep these in sync
     * with the identical constants in {@link IZombieOnlineUiModal}.
     */
    private static final float POPUP_OFFSET_X = -10f;
    private static final float POPUP_OFFSET_Y = -120f;

    private final Skin skin;
    private final BiConsumer<QuickChatMessage.Kind, Integer> onSend;
    private final Runnable onRaise;
    private final Texture[] emojiTextures = new Texture[EMOJI_PATHS.length];
    private final Table popupLayer = new Table();
    private final List<Texture> ownedTextures = new java.util.ArrayList<>();
    private final Table picker;

    public QuickChatUi(BiConsumer<QuickChatMessage.Kind, Integer> onSend, Runnable onRaise) {
        this.onSend = onSend;
        this.onRaise = onRaise;
        this.skin = PvzSkin.get();

        setFillParent(true);
        bottom().right();

        for (int i = 0; i < EMOJI_PATHS.length; i++) {
            emojiTextures[i] = loadEmojiTexture(EMOJI_PATHS[i]);
        }

        // Chat-bubble layer, pinned above the toggle button with a clearance so
        // the first bubble never sticks to it. Stays behind the picker box.
        popupLayer.setFillParent(true);
        popupLayer.bottom().right();
        popupLayer.setTouchable(Touchable.disabled);
        popupLayer.pad(0f, 0f, CHAT_STACK_TOP + FIRST_BUBBLE_CLEARANCE + POPUP_OFFSET_Y,
                TOGGLE_PAD_RIGHT + TOGGLE_SIZE + 20f - POPUP_OFFSET_X);
        addActor(popupLayer);

        // Picker window: hidden until the toggle button pushes it open. Always
        // front-most inside this panel (above bubbles and buttons).
        picker = buildPicker();
        picker.setVisible(false);
        addActor(picker);

        // Toggle button placed directly via a cell (right/bottom), so no
        // fullscreen overlay swallows clicks meant for the picker box.
        add(buildToggleButton()).size(TOGGLE_SIZE).padRight(TOGGLE_PAD_RIGHT).padBottom(TOGGLE_PAD_BOTTOM);
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

        // Rise from below its spot while fading in, hold, then fade out.
        bubble.setColor(1f, 1f, 1f, 0f);
        bubble.addAction(Actions.sequence(
                Actions.moveBy(0f, -14f, 0f),
                Actions.parallel(
                        Actions.moveBy(0f, 14f, 0.25f),
                        Actions.fadeIn(0.2f)),
                Actions.delay(BUBBLE_LIFETIME),
                Actions.fadeOut(0.5f),
                Actions.removeActor()));
    }

    /** True while the picker window is open. */
    public boolean isPickerVisible() {
        return picker.isVisible();
    }

    /** True when the picker (if open) covers the stage point (sx, sy). */
    public boolean pickerContains(float sx, float sy) {
        if (!picker.isVisible())
            return false;
        Vector2 min = picker.localToStageCoordinates(new Vector2(0f, 0f));
        Vector2 max = picker.localToStageCoordinates(new Vector2(picker.getWidth(), picker.getHeight()));
        return sx >= min.x && sx <= max.x && sy >= min.y && sy <= max.y;
    }

    public void dispose() {
        for (Texture t : ownedTextures) {
            t.dispose();
        }
        ownedTextures.clear();
    }

    /**
     * The picker window, styled like a Travel Log quest card: a
     * {@link BorderedTable} supplies the rounded brown frame, and a solid brown
     * rounded rect fills the middle (same colour as the quest card body). The
     * preset messages (text-box chips) sit on top, the emojis below.
     */
    private Table buildPicker() {
        Table msgRow = new Table();
        for (int i = 0; i < MESSAGES.length; i++) {
            msgRow.add(buildMessageButton(MESSAGES[i], i)).pad(2f);
        }

        Table emojiRow = new Table();
        for (int i = 0; i < EMOJI_PATHS.length; i++) {
            emojiRow.add(buildEmojiButton(i)).size(46f, 46f).pad(3f);
        }

        Table content = new Table();
        content.add(msgRow).row();
        content.add(emojiRow).padTop(4f).row();

        int innerW = Math.round(content.getPrefWidth()) + 60;
        int innerH = Math.round(content.getPrefHeight()) + 44;

        // Brown rounded interior exactly like a quest card; the BorderedTable
        // frame is drawn around it (its inner area starts at +17/+16).
        Image bg = new Image(roundedRectTexture(innerW, innerH, 20, new Color(0.36f, 0.24f, 0.12f, 1f)));
        bg.setBounds(17f, 16f, innerW, innerH);

        BorderedTable picker = new BorderedTable();
        picker.pad(16f, 17f, 16f, 17f);
        picker.setTouchable(Touchable.enabled);
        picker.addActor(bg);
        picker.add(content);
        picker.setSize(innerW + 34f, innerH + 32f);
        return picker;
    }

    /** Solid rounded-rect PNG helper, matching the Travel Log quest palette. */
    private Texture roundedRectTexture(int width, int height, int radius, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(color);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius - 1, radius, radius);
        pixmap.fillCircle(radius, height - radius - 1, radius);
        pixmap.fillCircle(width - radius - 1, height - radius - 1, radius);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** The small speech-bubble button that opens/closes the picker. */
    private Actor buildToggleButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        TextureRegion region = PvZ2.textureBank.region("IMAGE_UI_ALMANAC_GENERIC_LTE_IMAGE");
        if (region != null) {
            Drawable up = new TextureRegionDrawable(region);
            style.imageUp = up;
            style.imageDown = up;
            style.imageOver = up;
        }
        ImageButton button = new ImageButton(style);
        button.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean handled = super.touchDown(event, x, y, pointer, button);
                event.stop();
                return handled;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePicker();
            }
        });
        return button;
    }

private void togglePicker() {
        if (picker.isVisible()) {
            picker.setVisible(false);
            picker.clearActions();
        } else {
            positionPicker();
            picker.setColor(1f, 1f, 1f, 0f);
            picker.setVisible(true);
            picker.addAction(Actions.fadeIn(0.12f));
            picker.toFront();
            if (onRaise != null) {
                onRaise.run();
            }
        }
    }

    /** Parks the picker just above the toggle button, right-aligned to the corner. */
    private void positionPicker() {
        float buttonTop = TOGGLE_PAD_BOTTOM + TOGGLE_SIZE;
        float x = 1280f - PICKER_MARGIN - picker.getWidth();
        picker.setPosition(Math.max(0f, x), buttonTop + PICKER_CLEARANCE);
    }

    /**
     * One preset message as a chip that looks like the game's text boxes (same
     * background drawable + "big" font). Clicking it sends the message and
     * closes the picker.
     */
    private Actor buildMessageButton(String text, int index) {
        Drawable bg = textBoxBackground();
        Drawable over = textBoxFocusedBackground();

        Table slot = new Table();
        slot.setBackground(bg);
        slot.pad(3f, 8f, 3f, 8f);

        Label label = new Label(text, skin, "big");
        label.setFontScale(0.36f);
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
                hidePicker();
                onSend.accept(QuickChatMessage.Kind.TEXT, index);
            }
        });
        return slot;
    }

    /** One preset emoji as a button; clicking sends it and closes the picker. */
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
                hidePicker();
                onSend.accept(QuickChatMessage.Kind.EMOJI, index);
            }
        });
        return slot;
    }

    /** Message bubble shown for received chat (textbox look). */
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

    /** Emoji bubble: the emoji PNG scaled with {@code Scaling.fit} into a box. */
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

    private void hidePicker() {
        picker.setVisible(false);
        picker.clearActions();
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