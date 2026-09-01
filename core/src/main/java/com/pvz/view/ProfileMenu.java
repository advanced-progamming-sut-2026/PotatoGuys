package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.enums.AudioPaths;
import com.pvz.models.AppContext;
import com.pvz.models.games.seasons.Season;
import com.pvz.models.user.User;
import com.pvz.utils.AvatarImages;
import com.pvz.utils.SaveManager;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class ProfileMenu extends ScreenAdapter {
    private static final Color TEXT_COLOR = new Color(0.28f, 0.18f, 0.08f, 1f);
    private static final Color LABEL_COLOR = new Color(0.55f, 0.38f, 0.18f, 1f);
    private static final Color SUBTITLE_COLOR = new Color(0.9f, 0.95f, 1f, 1f);

    private static final String AVATAR_DIR = "textures/avatars/";
    private static final String DEFAULT_AVATAR = AVATAR_DIR + "avatar_luffy.png";

    private static final List<String> AVAILABLE_AVATARS = List.of(
        "avatar_luffy.png",
        "avatar_zoro.png",
        "avatar_nami.png",
        "avatar_usopp.png",
        "avatar_sanji.png",
        "avatar_chopper.png",
        "avatar_robin.png",
        "avatar_franky.png",
        "avatar_brook.png",
        "avatar_jinbe.png"
    );

    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private User currentUser;

    private Image avatarImage;
    private Image avatarRing;
    private Table avatarPicker;
    private Texture dimTexture;

    public ProfileMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        MenuUiKit.installClickSound(stage);
        skin = PvzSkin.get();

        MenuUiKit.installRotatingBackground(stage.getRoot(), game.getGlobalAssetManager());

        currentUser = AppContext.getInstance().getCurrentUser();

        Stack frameStack = new Stack();
        BorderedTable mainPanel = new BorderedTable();
        mainPanel.top();
        mainPanel.setSize(1150, 900);
        mainPanel.setPosition((1920 - 1150) / 2, (1080 - 900) / 2);
        mainPanel.pad(35, 50, 50, 50);
        frameStack.add(mainPanel);

        // --- Close button overlay (top-right corner of the panel, like News) ----
        ImageButton closeBtn = new ImageButton(skin, "generic_close_circle");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new MainMenu(game));
            }
        });
        Table exitBtnOverlay = new Table();
        exitBtnOverlay.top().right();
        exitBtnOverlay.add(closeBtn).size(48).pad(10);
        frameStack.add(exitBtnOverlay);

        frameStack.setSize(1150, 900);
        frameStack.setPosition((1920 - 1150) / 2, (1080 - 900) / 2);
        stage.addActor(frameStack);

        // --- Header bar ---------------------------------------------------
        Table headerBar = new Table();
        headerBar.setBackground(skin.getDrawable("image_ui_mainmenu_mm_settings_tab_10"));
        headerBar.pad(16, 40, 16, 40);

        Table titleCol = new Table();
        Label title = new Label("PROFILE", skin, "big_outline");
        title.setFontScale(1.1f);
        Label subtitle = new Label("Your stats & personal info", skin, "medium");
        subtitle.setColor(SUBTITLE_COLOR);
        titleCol.add(title).row();
        titleCol.add(subtitle);

        headerBar.add(titleCol).center().expandX();
        mainPanel.add(headerBar).width(1010).row();

        // --- Content -------------------------------------------------------
        Table content = new Table();
        content.top().padTop(30);
        mainPanel.add(content).width(1010).expand().fill().row();

        // left column : avatar + identity
        Table leftCol = new Table();
        leftCol.top();

        Table avatarStack = createAvatarStack();
        leftCol.add(avatarStack).size(230, 230).row();

        TextButton changeAvatarBtn = new TextButton("Change Avatar", skin, "green");
        changeAvatarBtn.getLabel().setFontScale(1.0f);
        changeAvatarBtn.getLabel().setColor(Color.BLACK);
        changeAvatarBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                avatarPicker.setVisible(true);
            }
        });
        leftCol.add(changeAvatarBtn).width(240).height(52).padTop(16).row();

        if (currentUser != null) {
            Label name = new Label(currentUser.getNickName(), skin, "big");
            name.setFontScale(1.2f);
            name.setColor(TEXT_COLOR);
            leftCol.add(name).padTop(22).row();

            Label userLabel = new Label("@" + currentUser.getUsername(), skin, "medium");
            userLabel.setFontScale(0.9f);
            userLabel.setColor(LABEL_COLOR);
            leftCol.add(userLabel).padTop(6).row();

            Label emailLabel = new Label(currentUser.getEmail(), skin, "medium");
            emailLabel.setFontScale(0.85f);
            emailLabel.setColor(LABEL_COLOR);
            leftCol.add(emailLabel).padTop(6);
        }

        // right column : stats cards
        Table rightCol = new Table();
        rightCol.top().padLeft(40);
        Label statsTitle = new Label("STATISTICS", skin, "big_outline");
        statsTitle.setFontScale(0.8f);
        rightCol.add(statsTitle).left().padBottom(18).row();

        Table statsGrid = new Table();
        statsGrid.defaults().space(16);
        rightCol.add(statsGrid);

        if (currentUser != null) {
            statsGrid.add(createStatCard("Games Played",
                    String.valueOf(currentUser.getProfile().getGamePlayed()))).size(220, 120);
            statsGrid.add(createStatCard("Completed Levels",
                    String.valueOf(completedLevels()))).size(220, 120);
            statsGrid.add(createStatCard("Highest Miopoint",
                    String.valueOf(currentUser.getProfile().getMaxMiopoint()))).size(220, 120).row();

            statsGrid.add(createStatCard("Coins",
                    String.valueOf(currentUser.getProfile().getCoins()))).size(220, 120);
            statsGrid.add(createStatCard("Diamonds",
                    String.valueOf(currentUser.getProfile().getDiamonds()))).size(220, 120);
            statsGrid.add(createStatCard("Plant Food",
                    String.valueOf(currentUser.getProfile().getPlantFood()))).size(220, 120).row();
        }

        content.add(leftCol).width(300).top();
        content.add(rightCol).expandX().top();

        // --- Action buttons ------------------------------------------------
        Table buttonRow = new Table();
        buttonRow.padTop(30);

        TextButton changeInfoBtn = new TextButton("Change Info", skin, "green");
        changeInfoBtn.getLabel().setFontScale(1.1f);
        changeInfoBtn.getLabel().setColor(Color.BLACK);
        changeInfoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ProfileEditMenu(game));
            }
        });

        TextButton changePasswordBtn = new TextButton("Change Password", skin, "brown");
        changePasswordBtn.getLabel().setFontScale(1.1f);
        changePasswordBtn.getLabel().setColor(Color.BLACK);
        changePasswordBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new PasswordChangeMenu(game));
            }
        });

        buttonRow.add(changeInfoBtn).width(230).height(58).padRight(20);
        buttonRow.add(changePasswordBtn).width(230).height(58);

        mainPanel.add(buttonRow).width(1010).row();

        // --- Avatar picker overlay -----------------------------------------
        buildAvatarPicker();

        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,AudioManager.getInstance().getUserMusicVolume());
    }

    private static String getAvatarPath(String avatarName) {
        if (avatarName == null || avatarName.isEmpty()) {
            return DEFAULT_AVATAR;
        }
        String path = avatarName;
        if (!path.startsWith("textures/")) {
            path = AVATAR_DIR + avatarName;
        }
        if (!path.endsWith(".png")) {
            path = path + ".png";
        }
        return path;
    }

    private static Texture getAvatarTexture(String avatarName) {
        return AvatarImages.getTexture(getAvatarPath(avatarName));
    }

    private static List<String> listAvatarPaths() {
        List<String> paths = new ArrayList<>();
        for (String name : AVAILABLE_AVATARS) {
            String path = AVATAR_DIR + name;
            if (Gdx.files.internal(path).exists()) {
                paths.add(path);
            }
        }
        return paths;
    }

    private int completedLevels() {
        int count = 0;
        for (Season season : currentUser.getProfile().getSeasons()) {
            count += season.getUnlockedLevelCount();
        }
        return count;
    }

    private Table createAvatarStack() {
        Texture avatarTex = getAvatarTexture(
            currentUser != null ? currentUser.getProfilePicture() : null);
        Texture feathered = AvatarImages.featheredCircle(avatarTex);
        avatarImage = new Image(new TextureRegionDrawable(feathered));

        avatarRing = new Image(skin.getDrawable("image_ui_hud_ingame_alert_ring"));

        Stack stack = new Stack();
        Table ringCell = new Table();
        ringCell.add(avatarRing).size(230, 230);
        stack.add(ringCell);

        Table avatarCell = new Table();
        avatarCell.add(avatarImage).size(196, 196);
        stack.add(avatarCell);

        Table stackTable = new Table();
        stackTable.add(stack).size(230, 230);
        return stackTable;
    }

    private void updateAvatarImage(String path) {
        Texture feathered = AvatarImages.featheredCircle(AvatarImages.getTexture(path));
        avatarImage.setDrawable(new TextureRegionDrawable(feathered));
    }

    private Table createStatCard(String labelText, String valueText) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        card.center();

        Label value = new Label(valueText, skin, "big");
        value.setFontScale(1.2f);
        value.setColor(TEXT_COLOR);

        Label label = new Label(labelText, skin, "medium");
        label.setFontScale(0.75f);
        label.setColor(LABEL_COLOR);

        card.add(value).row();
        card.add(label).padTop(6);
        return card;
    }

    private void buildAvatarPicker() {
        avatarPicker = new Table();
        avatarPicker.setFillParent(true);
        avatarPicker.center();
        avatarPicker.setVisible(false);

        Pixmap dimPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPix.setColor(0, 0, 0, 0.6f);
        dimPix.fill();
        dimTexture = new Texture(dimPix);
        dimPix.dispose();
        avatarPicker.setBackground(new TextureRegionDrawable(dimTexture));

        BorderedTable pickerContent = new BorderedTable();
        pickerContent.top();
        pickerContent.setSize(1000, 760);

        Table headerBar = new Table();
        headerBar.setBackground(skin.getDrawable("image_ui_mainmenu_mm_settings_tab_10"));
        headerBar.pad(16, 40, 16, 40);
        Label title = new Label("CHOOSE YOUR AVATAR", skin, "big_outline");
        title.setFontScale(1.0f);
        headerBar.add(title).left();
        pickerContent.add(headerBar).width(860).row();

        Table grid = new Table();
        grid.top();
        List<String> avatars = listAvatarPaths();
        int cols = 5;
        for (int i = 0; i < avatars.size(); i++) {
            final String path = avatars.get(i);
            Texture tex = AvatarImages.getTexture(path);
            ImageButton btn = new ImageButton(new TextureRegionDrawable(tex));
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    currentUser.setProfilePicture(path);
                    currentUser.saveUser();
                    updateAvatarImage(path);
                    avatarPicker.setVisible(false);
                }
            });
            grid.add(btn).size(140, 140).pad(12);
            if ((i + 1) % cols == 0) {
                grid.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        pickerContent.add(scrollPane).size(880, 560).padTop(20).row();

        // --- Close button overlay (top-right corner of the panel, like News) ----
        ImageButton pickerCloseBtn = new ImageButton(skin, "generic_close");
        pickerCloseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                avatarPicker.setVisible(false);
            }
        });
        Table exitBtnOverlay = new Table();
        exitBtnOverlay.top().right();
        exitBtnOverlay.add(pickerCloseBtn).size(48).pad(10);
        Stack pickerStack = new Stack();
        pickerStack.add(pickerContent);
        pickerStack.add(exitBtnOverlay);
        pickerStack.setSize(1000, 760);
        avatarPicker.add(pickerStack).size(1000, 760);
        stage.addActor(avatarPicker);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (dimTexture != null) {
            dimTexture.dispose();
        }
        stage.dispose();
        skin.dispose();
        AvatarImages.dispose();
    }
}
