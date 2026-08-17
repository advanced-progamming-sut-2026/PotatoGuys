package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pvz.models.AppContext;
import com.pvz.models.user.Message;
import com.pvz.models.user.User;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class NewsModal extends Table {

    private static final Color READ_COLOR = new Color(0.28f, 0.18f, 0.08f, 1f);
    private static final Color UNREAD_COLOR = new Color(0.75f, 0.12f, 0.05f, 1f);

    private static final float MODAL_WIDTH = 980f;
    private static final float MODAL_HEIGHT = 720f;

    private static final String WELCOME_TEXT =
            "[NEW] Welcome to PvZ! Your account has been successfully created. Stay tuned for more news.";

    private Label unreadBadge;
    private final Table messageList;
    private final TextButton unreadTab;
    private final TextButton allTab;
    private Skin skin;

    private boolean showingUnread = true;

    public NewsModal() {
        super();
        center();
        setVisible(false);

        skin = PvzSkin.get();

        BorderedTable newsContent = new BorderedTable();
        newsContent.top();
        newsContent.pad(30, 40, 40, 40);

        // --- Header bar ----------------------------------------------------
        Table headerBar = new Table();
        headerBar.setBackground(skin.getDrawable("image_ui_mainmenu_mm_settings_tab_10"));
        headerBar.pad(16, 40, 16, 40);

        Texture iconTex = new Texture(Gdx.files.internal("textures/ui/news_button.png"));
        iconTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Image newsIcon = new Image(iconTex);
        newsIcon.setSize(70, 70);

        Table titleCol = new Table();
        Label title = new Label("NEWS", skin, "big_outline");
        title.setFontScale(1.1f);
        Label subtitle = new Label("The latest from the plant vs zombie world", skin, "medium");
        subtitle.setColor(new Color(0.9f, 0.95f, 1f, 1f));
        titleCol.add(title).row();
        titleCol.add(subtitle);

        headerBar.add(newsIcon).size(70).padRight(24);
        headerBar.add(titleCol).left();

        newsContent.add(headerBar).width(860).row();

        // --- Tabs (Unread / All) -------------------------------------------
        TextButton.TextButtonStyle tabStyle = new TextButton.TextButtonStyle();
        tabStyle.up = skin.getDrawable("image_ui_generic_bluetab_down");
        tabStyle.over = skin.getDrawable("image_ui_generic_bluetab_down");
        tabStyle.checked = skin.getDrawable("image_ui_generic_bluetab_active");
        tabStyle.font = skin.getFont("FBUSV8C5EI_2");
        tabStyle.fontColor = new Color(0.18f, 0.18f, 0.18f, 1f);
        tabStyle.checkedFontColor = Color.WHITE;

        unreadTab = new TextButton("Unread News", tabStyle);
        allTab = new TextButton("All News", tabStyle);

        ButtonGroup<TextButton> tabGroup = new ButtonGroup<>();
        tabGroup.setMinCheckCount(1);
        tabGroup.setMaxCheckCount(1);
        tabGroup.add(unreadTab, allTab);

        unreadTab.setChecked(true);

        unreadTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (unreadTab.isChecked() && !showingUnread) {
                    showingUnread = true;
                    rebuildList();
                }
            }
        });
        allTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (allTab.isChecked() && showingUnread) {
                    showingUnread = false;
                    rebuildList();
                }
            }
        });

        Table tabsTable = new Table();
        tabsTable.add(unreadTab).size(260, 78).padRight(30);
        tabsTable.add(allTab).size(260, 78);
        newsContent.add(tabsTable).padTop(24).row();

        // --- Scrollable news list ------------------------------------------
        messageList = new Table();
        messageList.top();

        ScrollPane scrollPane = new ScrollPane(messageList, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        newsContent.add(scrollPane).expand().fill().padTop(20).row();

        // --- Close button overlay -------------------------------------------
        ImageButton exitBtn = new ImageButton(skin, "generic_close");
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                setVisible(false);
            }
        });

        Stack frameStack = new Stack();
        frameStack.add(newsContent);

        Table exitBtnOverlay = new Table();
        exitBtnOverlay.top().right();
        exitBtnOverlay.add(exitBtn).size(48).pad(10);
        frameStack.add(exitBtnOverlay);

        add(frameStack).size(MODAL_WIDTH, MODAL_HEIGHT);
    }

    public void showNews() {
        setVisible(true);
        showingUnread = true;
        unreadTab.setChecked(true);
        allTab.setChecked(false);
        rebuildList();
    }

    private void rebuildList() {
        messageList.clearChildren();

        User user = AppContext.getInstance().getCurrentUser();
        if (user == null || user.getProfile() == null || user.getProfile().getNews() == null) {
            addEmptyRow("No user is currently logged in.");
            return;
        }

        List<Message> messages = user.getProfile().getNews().getMessages();
        if (messages.isEmpty()) {
            addEmptyRow(WELCOME_TEXT);
            return;
        }

        List<Message> toShow = new ArrayList<>();
        if (showingUnread) {
            for (Message msg : messages) {
                if (msg.isUnread()) {
                    toShow.add(msg);
                }
            }
            if (toShow.isEmpty()) {
                addEmptyRow("No unread news.");
                return;
            }
            for (Message msg : toShow) {
                addNewsRow(msg, true);
                msg.setUnread(false);
            }
            user.saveUser();
        } else {
            for (Message msg : messages) {
                addNewsRow(msg, msg.isUnread());
            }
        }

        updateUnreadBadge();
    }

    private void addNewsRow(Message msg, boolean unread) {
        Table item = new Table();
        item.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        item.pad(18, 26, 18, 26);

        Label label = new Label((unread ? "[NEW] " : "[READ] ") + msg.getMessage(), skin, "medium");
        label.setWrap(true);
        label.setFontScale(0.95f);
        label.setColor(unread ? UNREAD_COLOR : READ_COLOR);

        item.add(label).width(760).left();
        messageList.add(item).width(860).padBottom(14).row();
    }

    private void addEmptyRow(String text) {
        Table item = new Table();
        item.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        item.pad(18, 26, 18, 26);

        Label label = new Label(text, skin, "medium");
        label.setWrap(true);
        label.setFontScale(0.95f);
        label.setColor(READ_COLOR);

        item.add(label).width(760).left();
        messageList.add(item).width(860).padBottom(14).row();
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
