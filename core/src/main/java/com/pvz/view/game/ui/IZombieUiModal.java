package com.pvz.view.game.ui;

import java.util.function.BiConsumer;

import com.pvz.models.AppContext;
import com.pvz.network.game.QuickChatMessage;

/**
 * In-game HUD specialised for {@code IZombieMode} (the online I,Zombie game).
 *
 * <p>
 * Reuses the entire global HUD from {@link GameUiModal} (top bar, sun bank,
 * plant-food dots, wallet, wave meter, shovel, pause) and, when an online
 * match is running, adds the bottom-right quick-chat panel on top of it.
 *
 * <p>
 * The chat lives here instead of in the base HUD so that normal levels never
 * render the chat/emoji bar — it only exists in I,Zombie matches.
 */
public class IZombieUiModal extends GameUiModal {

    private final QuickChatUi quickChatUi;

    public IZombieUiModal(Runnable onPauseRequested,
                          BiConsumer<QuickChatMessage.Kind, Integer> onChatSend) {
        super(onPauseRequested);

        // Quick chat is an online-match feature; a single-player I,Zombie level
        // has no opponent to talk to, so the bar is skipped there.
        if (AppContext.getInstance().getMatchSession() == null) {
            quickChatUi = null;
        } else {
            quickChatUi = new QuickChatUi(onChatSend);
            addActor(quickChatUi);
        }
    }

    /** Pops a received chat bubble (text or emoji) at the bottom-right. */
    public void showChat(QuickChatMessage msg) {
        if (quickChatUi != null) {
            quickChatUi.showChat(msg);
        }
    }

    /** True while the quick-chat picker window is open. */
    public boolean isChatPickerVisible() {
        return quickChatUi != null && quickChatUi.isPickerVisible();
    }

    /** True when the open quick-chat picker covers the stage point (x, y). */
    public boolean chatPickerContains(float x, float y) {
        return quickChatUi != null && quickChatUi.pickerContains(x, y);
    }

    /** Releases the chat textures owned by the quick-chat panel. */
    public void disposeChat() {
        if (quickChatUi != null) {
            quickChatUi.dispose();
        }
    }
}