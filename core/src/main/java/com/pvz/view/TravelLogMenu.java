package com.pvz.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.controller.QuestController;
import com.pvz.enums.AudioPaths;
import com.pvz.enums.GameAsset;
import com.pvz.models.quests.Quest;
import com.pvz.models.quests.QuestCategory;
import com.pvz.models.quests.QuestPriority;

import com.pvz.view.game.GameScreen;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Travel Log menu: shows Daily / Main / Epic / All quests with progress and
 * claimable rewards, plus a Minigames tab to jump straight into a mini-game
 * level. Backed by {@link QuestController}, which reads/writes the current
 * user's QuestLog.
 */
public class TravelLogMenu extends ScreenAdapter {

    private enum Tab { DAILY, MAIN, EPIC, ALL, MINIGAMES }

    private static final String[] MINI_GAME_SEASON_FOLDERS = { "VaseBreaker", "Wallnut Bowling", "IZombie" };
    private static final String[] MINI_GAME_LABELS = { "Vasebreaker", "Wallnut Bowling", "I, Zombie" };
    private static final String[] MINI_GAME_WALLPAPERS = {
        "textures/backgrounds/VASEBREAKER/wallpaper.png",
        "textures/backgrounds/WALLNUTBOWLING/wallpaper.png",
        "textures/backgrounds/IZOMBIE/wallpaper.png"
    };
    private static final float BAR_WIDTH = 420f;
    private static final float BAR_HEIGHT = 20f;
    private static final float CARD_WIDTH = 1000f;

    private final PvZ2 game;
    private final QuestController controller;

    private Stage stage;
    private Skin skin;
    private Table tabsTable;
    private Table listTable;
    private Label coinsLabel;
    private Label diamondsLabel;
    private Label resetTimerLabel;

    private Texture barBgTexture;
    private Texture barFillTexture;
    private Texture darkBgTexture;
    private Texture cardBgTexture;

    private final Map<String, Texture> wallpaperCache = new HashMap<>();

    private Tab currentTab = Tab.DAILY;

    public TravelLogMenu(PvZ2 game) {
        this.game = game;
        this.controller = new QuestController();
    }

    @Override
    public void show() {
        super.show();
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        barBgTexture = solidTexture(new Color(0f, 0f, 0f, 0.45f));
        barFillTexture = solidTexture(new Color(0.30f, 0.75f, 0.30f, 1f));

        MenuUiKit.installRotatingBackground(stage.getRoot(), game.getGlobalAssetManager());

        BorderedTable mainPanel = new BorderedTable();
        mainPanel.setSize(1200, 1000);
        mainPanel.setPosition((1920 - 1200) / 2f, (1080 - 1000) / 2f);
        stage.addActor(mainPanel);

        darkBgTexture = roundedRectTexture(1166, 968, 20, new Color(0.24f, 0.14f, 0.06f, 1f));
        Image darkBg = new Image(darkBgTexture);
        darkBg.setBounds(17, 16, 1166, 968);
        mainPanel.addActor(darkBg);

        cardBgTexture = roundedRectTexture(966, 168, 20, new Color(0.36f, 0.24f, 0.12f, 1f));

        mainPanel.add(buildTopBar()).fillX().padTop(15).padLeft(25).padRight(25).row();

        tabsTable = new Table();
        mainPanel.add(tabsTable).padTop(15).row();

        Label.LabelStyle resetStyle = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.valueOf("8FCE7E"));
        resetTimerLabel = new Label("", resetStyle);
        resetTimerLabel.setFontScale(1.1f);
        mainPanel.add(resetTimerLabel).padTop(6).row();

        listTable = new Table();
        listTable.top();
        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        mainPanel.add(scrollPane).width(CARD_WIDTH + 40).height(720).padTop(10).padBottom(15).row();

        buildTabs();
        refreshWallet();
        refreshList();

        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,AudioManager.getInstance().getUserMusicVolume());
    }

    private Table buildTopBar() {
        Table top = new Table();

        ImageButton backBtn = new ImageButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new MainMenu(game));
            }
        });

        Label titleLabel = new Label("TRAVEL LOG", skin, "big");
        titleLabel.setFontScale(1.4f);
        titleLabel.setColor(Color.valueOf("F1E4C0"));

        top.add(backBtn).size(75, 70).left();
        top.add(titleLabel).expandX().center();
        top.add(buildWallet()).right();
        return top;
    }

    private Table buildWallet() {
        Table table = new Table();
        Label.LabelStyle descStyle = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.valueOf("F1E4C0"));

        Image coinIcon = new Image(skin.getDrawable("image_ui_generic_coin_icon_small"));
        coinsLabel = new Label("0", descStyle);
        coinsLabel.setFontScale(1.2f);

        Image gemIcon = new Image(skin.getDrawable("image_ui_generic_gem_icon_small"));
        diamondsLabel = new Label("0", descStyle);
        diamondsLabel.setFontScale(1.2f);

        table.add(coinIcon).size(32, 32);
        table.add(coinsLabel).padLeft(6).padRight(25);
        table.add(gemIcon).size(32, 42);
        table.add(diamondsLabel).padLeft(6);
        return table;
    }

    private void refreshWallet() {
        coinsLabel.setText(String.valueOf(controller.getCoins()));
        diamondsLabel.setText(String.valueOf(controller.getDiamonds()));
    }

    private void buildTabs() {
        tabsTable.clearChildren();
        for (Tab tab : Tab.values()) {
            tabsTable.add(tabButton(tab)).width(170).height(50).padLeft(4).padRight(4);
        }
    }

    private void updateResetTimer() {
        if (resetTimerLabel == null) return;
        boolean showTimer = currentTab == Tab.DAILY || currentTab == Tab.ALL;
        resetTimerLabel.setVisible(showTimer);
        if (showTimer) {
            resetTimerLabel.setText("Daily quests reset in " + controller.getMillisUntilDailyResetFormatted());
        }
    }

    private TextButton tabButton(Tab tab) {
        String style = tab == currentTab ? "purple" : "brown";
        TextButton button = new TextButton(tabLabel(tab), skin, style);
        button.getLabel().setFontScale(1.05f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                currentTab = tab;
                buildTabs();
                refreshList();
            }
        });
        return button;
    }

    private String tabLabel(Tab tab) {
        return switch (tab) {
            case DAILY -> "Daily";
            case MAIN -> "Main";
            case EPIC -> "Epic";
            case ALL -> "All";
            case MINIGAMES -> "Minigames";
        };
    }

    private void refreshList() {
        listTable.clearChildren();
        updateResetTimer();

        if (currentTab == Tab.MINIGAMES) {
            buildMinigamesList();
            return;
        }

        List<Quest> quests = currentTab == Tab.ALL
            ? controller.getAllQuests()
            : controller.getQuests(QuestCategory.valueOf(currentTab.name()));

        if (quests.isEmpty()) {
            Label empty = new Label("No quests here right now.", skin);
            empty.setColor(Color.valueOf("D8C9A8"));
            listTable.add(empty).pad(20).row();
            return;
        }

        for (Quest quest : quests) {
            listTable.add(buildQuestCard(quest)).width(CARD_WIDTH).height(200).padBottom(14).row();
        }
    }

    private Table buildQuestCard(Quest quest) {
        BorderedTable card = new BorderedTable();
        card.pad(10).padLeft(40).padRight(40);
        Image cardBg = new Image(cardBgTexture);
        cardBg.setBounds(17, 16, 966, 168);
        card.addActor(cardBg);
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.getFont("FBUSV8C5EI_1"), Color.valueOf("F1E4C0"));
        Label.LabelStyle descStyle = new Label.LabelStyle(skin.getFont("FBUSV8C5EI_2"), Color.valueOf("F1E4C0"));

        Table header = new Table();
        Label titleLabel = new Label(quest.getTitle(), titleStyle);
        titleLabel.setFontScale(1.0f);
        header.add(titleLabel).left().expandX();
        header.add(priorityBadge(quest.getPriority())).right();
        card.add(header).fillX().row();

        Label descLabel = new Label(quest.getDescription(), descStyle);
        descLabel.setFontScale(1.1f);
        descLabel.setColor(Color.valueOf("D8C9A8"));
        descLabel.setWrap(true);
        card.add(descLabel).width(CARD_WIDTH - 100).left().padTop(6).row();

        Table progressRow = new Table();
        progressRow.add(buildProgressBar(quest)).size(BAR_WIDTH, BAR_HEIGHT).left();
        Label progressLabel = new Label(quest.getProgress().toString(), descStyle);
        progressLabel.setFontScale(1.0f);
        progressRow.add(progressLabel).padLeft(10);
        card.add(progressRow).left().padTop(4).row();

        Table footer = new Table();
        Label rewardLabel = new Label("Reward: " + quest.getRewardDescription(), descStyle);
        rewardLabel.setFontScale(1.0f);
        rewardLabel.setColor(Color.valueOf("8FCE7E"));
        footer.add(rewardLabel).left().expandX();
        footer.add(statusWidget(quest)).width(120).right();
        card.add(footer).fillX().padTop(0).row();

        return card;
    }

    private Label priorityBadge(QuestPriority priority) {
        Color color = switch (priority) {
            case CRITICAL -> Color.valueOf("FF5252");
            case HIGH -> Color.valueOf("FFA726");
            case MEDIUM -> Color.valueOf("FFD54F");
            case LOW -> Color.valueOf("BDBDBD");
        };
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("FBUSV8C5EI_2"), color);
        Label badge = new Label(priority.name(), style);
        badge.setFontScale(1.0f);
        return badge;
    }

    private Actor buildProgressBar(Quest quest) {
        Stack barStack = new Stack();
        barStack.add(new Image(barBgTexture));

        int target = Math.max(1, quest.getProgress().getTargetAmount());
        int current = Math.min(quest.getProgress().getCurrentAmount(), target);
        float ratio = current / (float) target;

        Table fillRow = new Table();
        fillRow.left();
        fillRow.add(new Image(barFillTexture)).width(Math.max(4f, BAR_WIDTH * ratio)).height(BAR_HEIGHT);
        barStack.add(fillRow);
        return barStack;
    }

    private Actor statusWidget(Quest quest) {
        if (quest.isClaimed()) {
            Label.LabelStyle claimedStyle = new Label.LabelStyle(skin.getFont("FBUSV8C5EI_2"), Color.valueOf("6B6B6B"));
            Label label = new Label("Already received", claimedStyle);
            label.setFontScale(1.0f);
            return label;
        }

        if (quest.isCompleted()) {
            TextButton btn = new TextButton("Claim", skin, "green_small");
            btn.getLabel().setFontScale(1.1f);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    controller.claimQuest(quest.getId());
                    refreshWallet();
                    refreshList();
                }
            });
            return btn;
        }

        Stack grayStack = new Stack();
        grayStack.add(new Image(roundedRectTexture(120, 36, 12, Color.valueOf("555555"))));
        grayStack.add(new Image(roundedRectOutline(120, 36, 12, 2, Color.valueOf("888888"))));
        Label lbl = new Label("Claim", new Label.LabelStyle(skin.getFont("FBUSV8C5EI_2"), Color.valueOf("999999")));
        lbl.setFontScale(1.1f);
        Table content = new Table();
        content.add(lbl);
        grayStack.add(content);
        return grayStack;
    }

    private void buildMinigamesList() {
        for (int i = 0; i < MINI_GAME_SEASON_FOLDERS.length; i++) {
            listTable.add(buildMinigameCard(MINI_GAME_SEASON_FOLDERS[i], MINI_GAME_LABELS[i], MINI_GAME_WALLPAPERS[i]))
                .width(CARD_WIDTH).height(300).padBottom(14).row();
        }
    }

    /**
     * Loads a wallpaper, scales it to width x height, cuts the corners into
     * rounded arcs and draws a narrow frame around the card. The result is
     * cached per path so tab switches do not re-process the image.
     */
    private Image buildWallpaperImage(String path, int width, int height, int radius, int frameWidth) {
        Texture cached = wallpaperCache.get(path);
        if (cached != null) {
            return new Image(cached);
        }

        Texture source = MenuUiKit.loadTextureSafe(path);
        TextureData data = source.getTextureData();
        if (!data.isPrepared()) {
            data.prepare();
        }
        Pixmap sourcePixmap = data.consumePixmap();
        data.disposePixmap();
        source.dispose();

        Pixmap scaled = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        scaled.setFilter(Pixmap.Filter.BiLinear);
        scaled.drawPixmap(sourcePixmap, 0, 0, sourcePixmap.getWidth(), sourcePixmap.getHeight(), 0, 0, width, height);
        sourcePixmap.dispose();

        Color frameColor = new Color(0xC9A227FF);
        int frameBits = frameColor.toIntBits();
        int innerRadius = radius - frameWidth;
        int innerRadius2 = innerRadius * innerRadius;
        int outerRadius2 = radius * radius;

        Pixmap out = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean corner = (x < radius && y < radius)
                    || (x >= width - radius && y < radius)
                    || (x < radius && y >= height - radius)
                    || (x >= width - radius && y >= height - radius);
                if (corner) {
                    float dx;
                    float dy;
                    if (x < radius && y < radius) {
                        dx = x - radius;
                        dy = y - radius;
                    } else if (x >= width - radius && y < radius) {
                        dx = x - (width - radius);
                        dy = y - radius;
                    } else if (x < radius && y >= height - radius) {
                        dx = x - radius;
                        dy = y - (height - radius);
                    } else {
                        dx = x - (width - radius);
                        dy = y - (height - radius);
                    }
                    float distance2 = dx * dx + dy * dy;
                    if (distance2 > outerRadius2) {
                        out.drawPixel(x, y, 0);
                    } else if (distance2 > innerRadius2) {
                        out.drawPixel(x, y, frameBits);
                    } else {
                        out.drawPixel(x, y, scaled.getPixel(x, y));
                    }
                } else if (x < frameWidth || y < frameWidth
                    || x >= width - frameWidth || y >= height - frameWidth) {
                    out.drawPixel(x, y, frameBits);
                } else {
                    out.drawPixel(x, y, scaled.getPixel(x, y));
                }
            }
        }
        scaled.dispose();

        Texture texture = new Texture(out);
        out.dispose();
        wallpaperCache.put(path, texture);
        return new Image(texture);
    }

    private Table buildMinigameCard(String seasonFolder, String displayName, String wallpaperPath) {
        Label.LabelStyle descStyle = new Label.LabelStyle(skin.getFont("FBUSV8C5EI_1"), Color.BLACK);

        Label subLabel = new Label("Choose a level to play.", descStyle);
        subLabel.setFontScale(1.05f);
        subLabel.setColor(Color.valueOf("D8C9A8"));

        Table levels = new Table();
        for (int level = 1; level <= 3; level++) {
            int chosenLevel = level;
            TextButton levelBtn = new TextButton("Level " + level, skin, "brown");
            levelBtn.getLabel().setFontScale(1.0f);
            levelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    if (seasonFolder.equals("IZombie")) {
                        game.setScreen(new OpponentSelectMenu(game, seasonFolder, chosenLevel));
                    } else {
                        game.setScreen(new GameScreen(seasonFolder, chosenLevel));
                    }
                }
            });
            levels.add(levelBtn).size(140, 55).padLeft(6);
        }

        Table rightPanel = new Table();
        rightPanel.add(subLabel).padBottom(8).row();
        rightPanel.add(levels).row();

        // Wallpaper-backed card: the texture fills the card (rounded corners
        // and a narrow frame baked into it), with the "Choose a level to
        // play." label and the level buttons stacked on top, bottom-right.
        // Falls back to the plain bordered card when no wallpaper is
        // configured or the file is missing.
        if (wallpaperPath != null && Gdx.files.internal(wallpaperPath).exists()) {
            Image wallpaper = buildWallpaperImage(wallpaperPath, (int) CARD_WIDTH, 300, 28, 5);

            Table content = new Table();
            content.pad(14).padLeft(40).padRight(40);
            content.bottom().right();
            content.add(rightPanel).right();

            Stack stack = new Stack();
            stack.add(wallpaper);
            stack.add(content);

            Table wrapper = new Table();
            wrapper.add(stack).grow();
            return wrapper;
        }

        BorderedTable card = new BorderedTable();
        card.pad(14).padLeft(40).padRight(40);
        card.bottom().right();
        card.add(rightPanel).right();
        return card;
    }

    private Texture solidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

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

    private Texture roundedRectOutline(int width, int height, int radius, int lineW, Color color) {
        Pixmap pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(color);
        pm.fillCircle(radius, radius, radius);
        pm.fillCircle(width - radius - 1, radius, radius);
        pm.fillCircle(radius, height - radius - 1, radius);
        pm.fillCircle(width - radius - 1, height - radius - 1, radius);
        pm.fillRectangle(radius, 0, width - 2 * radius, height);
        pm.fillRectangle(0, radius, width, height - 2 * radius);
        pm.setColor(Color.CLEAR);
        int in = radius - lineW;
        if (in > 0) {
            pm.fillCircle(in, in, in);
            pm.fillCircle(width - in - 1, in, in);
            pm.fillCircle(in, height - in - 1, in);
            pm.fillCircle(width - in - 1, height - in - 1, in);
        }
        pm.fillRectangle(Math.max(radius, lineW), lineW, width - Math.max(radius, lineW) * 2, height - 2 * lineW);
        pm.fillRectangle(lineW, Math.max(radius, lineW), width - 2 * lineW, height - Math.max(radius, lineW) * 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (resetTimerLabel != null && resetTimerLabel.isVisible()) {
            updateResetTimer();
        }
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        barBgTexture.dispose();
        barFillTexture.dispose();
        darkBgTexture.dispose();
        cardBgTexture.dispose();
        for (Texture texture : wallpaperCache.values()) {
            texture.dispose();
        }
        wallpaperCache.clear();
    }
}
