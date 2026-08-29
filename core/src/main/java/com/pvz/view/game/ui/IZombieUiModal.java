package com.pvz.view.game.ui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.network.game.QuickChatMessage;
import com.pvz.network.game.StickerMessage;

/**
 * In-game HUD specialised for {@code IZombieMode} (the online I,Zombie game).
 *
 * <p>
 * Reuses the entire global HUD from {@link GameUiModal} (top bar, sun bank,
 * plant-food dots, wallet, wave meter, shovel, pause) and, when an online
 * match is running, adds the bottom-right quick-chat panel and the sticker
 * (emote) picker on top of it.
 *
 * <p>
 * Both panels live here instead of in the base HUD so that normal levels never
 * render the chat/emoji bar — they only exist in I,Zombie matches.
 */
public class IZombieUiModal extends GameUiModal {

    /**
     * Where received stickers pop up: bottom-right, clear of the button stack
     * (shovel at 15, chat toggle at 82, sticker toggle at 140 — all 50 wide,
     * right-pad 20 → top of the stack = 190, right edge of the stack = 1210).
     * The drawn clip's bottom-right corner is parked 40px left of the stack's
     * right edge and 20px above the stack's top, then nudged by
     * {@link #POPUP_OFFSET_X} / {@link #POPUP_OFFSET_Y}.
     */
    private static final float STACK_RIGHT = 1280f - 20f - 50f;
    private static final float STACK_TOP = 190f;
    private static final float STICKER_CLEARANCE_X = 40f;
    private static final float STICKER_CLEARANCE_Y = 20f;

    /**
     * Shared tuning for the pop-up items (stickers, chat bubbles, emoji
     * bubbles): shift everything 10px left and 120px down. Keep these in sync
     * with the identical constants in {@link QuickChatUi}.
     */
    private static final float POPUP_OFFSET_X = -10f;
    private static final float POPUP_OFFSET_Y = -120f;

    /** Slide-up entrance and fade-out exit of the sticker. */
    private static final float STICKER_RISE = 90f;
    private static final float STICKER_RISE_SECONDS = 0.4f;
    private static final float STICKER_FADE_IN_SECONDS = 0.2f;
    private static final float STICKER_FADE_OUT_SECONDS = 0.4f;

    private final QuickChatUi quickChatUi;
    private final StickerUi stickerUi;
    private final Table stickerLayer = new Table();

    public IZombieUiModal(Runnable onPauseRequested,
                          BiConsumer<QuickChatMessage.Kind, Integer> onChatSend,
                          Consumer<StickerMessage> onStickerSend) {
        super(onPauseRequested);

        // Quick chat and stickers are online-match features; a single-player
        // I,Zombie level has no opponent to talk to, so both are skipped there.
        if (AppContext.getInstance().getMatchSession() == null) {
            quickChatUi = null;
            stickerUi = null;
        } else {
            // Layer order (back to front): received stickers, then the two
            // panels (chat then sticker). A panel's picker box is raised to
            // the front whenever it opens (see raiseChatPanel/raiseStickerPanel),
            // so boxes always sit in front of buttons, stickers and bubbles.
            stickerLayer.setFillParent(true);
            stickerLayer.setTouchable(Touchable.disabled);
            addActor(stickerLayer);

            quickChatUi = new QuickChatUi(onChatSend, this::raiseChatPanel);
            addActor(quickChatUi);

            stickerUi = new StickerUi(onStickerSend, this::raiseStickerPanel);
            addActor(stickerUi);
        }
    }

    /** Brings the chat panel (and its open box) in front of the sticker panel. */
    private void raiseChatPanel() {
        if (quickChatUi != null) {
            quickChatUi.toFront();
        }
    }

    /** Brings the sticker panel (and its open box) in front of the chat panel. */
    private void raiseStickerPanel() {
        if (stickerUi != null) {
            stickerUi.toFront();
        }
    }

    /** Pops a received chat bubble (text or emoji) at the bottom-right. */
    public void showChat(QuickChatMessage msg) {
        if (quickChatUi != null) {
            quickChatUi.showChat(msg);
        }
    }

    /**
     * Plays the opponent's sticker: it rises from below its final spot,
     * bottom-right clear of the panel buttons, loops while visible, then fades
     * out. Total on-screen time ≈ {@link StickerMessage#seconds}.
     */
    public void showSticker(StickerMessage msg) {
        if (msg == null || msg.pamPath == null || stickerLayer == null)
            return;

        float scale = msg.scale > 0f ? msg.scale : 1f;
        PamClipActor sticker = new PamClipActor(msg.pamPath, msg.clip, scale, -1f);

        float bw = 390f;
        float bh = 390f;
        if (sticker.isAvailable()) {
            try {
                Rectangle b = PvZ2.pamPlayer.bounds(msg.pamPath, sticker.resolvedClip());
                if (b != null && b.width > 0 && b.height > 0) {
                    bw = b.width;
                    bh = b.height;
                }
            } catch (RuntimeException ignored) {
            }
        }

        float anchorRight = STACK_RIGHT - STICKER_CLEARANCE_X + POPUP_OFFSET_X;
        float anchorBottom = STACK_TOP + STICKER_CLEARANCE_Y + POPUP_OFFSET_Y;
        sticker.setBounds(anchorRight - bw * scale, anchorBottom, bw * scale, bh * scale);

        float total = msg.seconds > 0f ? msg.seconds : 3f;
        float rise = Math.max(0f, total - STICKER_RISE_SECONDS - STICKER_FADE_OUT_SECONDS);
        sticker.setColor(1f, 1f, 1f, 0f);
        sticker.addAction(Actions.sequence(
                Actions.moveBy(0f, -STICKER_RISE, 0f),
                Actions.parallel(
                        Actions.moveBy(0f, STICKER_RISE, STICKER_RISE_SECONDS),
                        Actions.fadeIn(STICKER_FADE_IN_SECONDS)),
                Actions.delay(rise),
                Actions.fadeOut(STICKER_FADE_OUT_SECONDS),
                Actions.removeActor()));

        stickerLayer.addActor(sticker);
    }

    /** True while the quick-chat picker window is open. */
    public boolean isChatPickerVisible() {
        return quickChatUi != null && quickChatUi.isPickerVisible();
    }

    /** True when the open quick-chat picker covers the stage point (x, y). */
    public boolean chatPickerContains(float x, float y) {
        return quickChatUi != null && quickChatUi.pickerContains(x, y);
    }

    /** True while the sticker box is open. */
    public boolean isStickerBoxVisible() {
        return stickerUi != null && stickerUi.isBoxVisible();
    }

    /** True when the open sticker box covers the stage point (x, y). */
    public boolean stickerBoxContains(float x, float y) {
        return stickerUi != null && stickerUi.boxContains(x, y);
    }

    /** Releases the textures owned by the quick-chat and sticker panels. */
    public void disposeChat() {
        if (quickChatUi != null) {
            quickChatUi.dispose();
        }
        if (stickerUi != null) {
            stickerUi.dispose();
        }
    }
}