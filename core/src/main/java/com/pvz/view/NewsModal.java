package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pvz.models.AppContext;
import com.pvz.models.user.Message;
import com.pvz.models.user.User;
import com.pvz.utils.SaveManager;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class NewsModal extends Table {

    // Dark, readable-on-parchment colors — the old code left read messages at the
    // skin's default label color, which is near-white and nearly invisible against
    // the cream background. That's the "ghost text" you were seeing.
    private static final Color READ_COLOR = new Color(0.28f, 0.18f, 0.08f, 1f);
    private static final Color UNREAD_COLOR = new Color(0.75f, 0.12f, 0.05f, 1f);

    private static final float MODAL_WIDTH = 900f;
    private static final float MODAL_HEIGHT = 700f;
    private static final float MESSAGE_LABEL_WIDTH = 780f;

    private Label unreadBadge;
    private final Table messageList;

    public NewsModal() {
        super();
        center();
        setVisible(false);

        BorderedTable newsContent = new BorderedTable();
        newsContent.top();
        newsContent.pad(30);

        Label titleLabel = new Label("News", PvzSkin.get(), "big");
        titleLabel.setColor(Color.BLACK);
        newsContent.add(titleLabel).padBottom(20).row();

        messageList = new Table();
        messageList.top();

        // Plain ScrollPane (no skin style) so a long news list scrolls instead of
        // overflowing/getting clipped — the original modal had ScrollPane imported
        // but never actually used it.
        ScrollPane scrollPane = new ScrollPane(messageList);
        scrollPane.setFadeScrollBars(false);
        newsContent.add(scrollPane).expand().fill().row();

        ImageButton exitBtn = new ImageButton(PvzSkin.get(), "generic_close");
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                setVisible(false);
            }
        });

        // Overlay the close button on the frame's top-right corner via a Stack,
        // instead of the old fixed-pixel padLeft(850) hack that broke as soon as
        // the modal size changed.
        Stack frameStack = new Stack();
        frameStack.add(newsContent);

        Table exitBtnOverlay = new Table();
        exitBtnOverlay.top().right();
        exitBtnOverlay.add(exitBtn).size(48).pad(10);
        frameStack.add(exitBtnOverlay);

        add(frameStack).size(MODAL_WIDTH, MODAL_HEIGHT);
    }

    public void showNews() {
        messageList.clearChildren();
        setVisible(true);

        User user = AppContext.getInstance().getCurrentUser();
        if (user != null && user.getProfile() != null && user.getProfile().getNews() != null) {
            for (Message msg : user.getProfile().getNews().getMessages()) {
                Label msgLabel = new Label(msg.getMessage().replace(" ", "  "), PvzSkin.get());
                msgLabel.setWrap(true);
                msgLabel.setFontScale(1.3f);
                msgLabel.setColor(msg.isUnread() ? UNREAD_COLOR : READ_COLOR);
                messageList.add(msgLabel).left().width(MESSAGE_LABEL_WIDTH).padBottom(14).row();
                msg.setUnread(false);
            }
            SaveManager.getInstance().save(user, "users/" + user.getId() + ".json");
        }

        updateUnreadBadge();
    }

    public void updateUnreadBadge() {
        if (unreadBadge == null) {
            return;
        }
        User user = AppContext.getInstance().getCurrentUser();
        boolean hasUnread = false;
        if (user != null && user.getProfile() != null && user.getProfile().getNews() != null) {
            for (Message msg : user.getProfile().getNews().getMessages()) {
                if (msg.isUnread()) {
                    hasUnread = true;
                    break;
                }
            }
        }
        unreadBadge.setVisible(hasUnread);
    }

    public void setUnreadBadge(Label badge) {
        this.unreadBadge = badge;
        updateUnreadBadge();
    }
}
