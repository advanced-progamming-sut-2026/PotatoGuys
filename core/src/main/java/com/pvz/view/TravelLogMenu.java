package com.pvz.view;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

    private Texture barBgTexture;
    private Texture barFillTexture;

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

        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        Texture bgTexture = GameAsset.MAIN_MENU_BG.get(game.getGlobalAssetManager());
        Image bgImage = new Image(bgTexture);
        stack.add(bgImage);

        BorderedTable mainPanel = new BorderedTable();
        mainPanel.setSize(1100, 940);
        mainPanel.setPosition((1920 - 1100) / 2f, (1080 - 940) / 2f);
        stack.add(mainPanel);

        mainPanel.add(buildTopBar()).fillX().padTop(15).padLeft(25).padRight(25).row();

        tabsTable = new Table();
        mainPanel.add(tabsTable).padTop(15).row();

        listTable = new Table();
        listTable.top();
        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        mainPanel.add(scrollPane).width(CARD_WIDTH + 40).height(680).padTop(10).padBottom(15).row();

        buildTabs();
        refreshWallet();
        refreshList();

        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,0.7f);
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
        titleLabel.setColor(Color.BLACK);

        top.add(backBtn).size(75, 70).left();
        top.add(titleLabel).expandX().center();
        top.add(buildWallet()).right();
        return top;
    }

    private Table buildWallet() {
        Table table = new Table();
        Label.LabelStyle descStyle = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.BLACK);

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

        if (currentTab == Tab.MINIGAMES) {
            buildMinigamesList();
            return;
        }

        List<Quest> quests = currentTab == Tab.ALL
            ? controller.getAllQuests()
            : controller.getQuests(QuestCategory.valueOf(currentTab.name()));

        if (quests.isEmpty()) {
            Label empty = new Label("No quests here right now.", skin);
            empty.setColor(Color.DARK_GRAY);
            listTable.add(empty).pad(20).row();
            return;
        }

        for (Quest quest : quests) {
            listTable.add(buildQuestCard(quest)).width(CARD_WIDTH).height(180).padBottom(14).row();
        }
    }

    private Table buildQuestCard(Quest quest) {
        BorderedTable card = new BorderedTable();
        card.pad(10).padLeft(40).padRight(40);
        Label.LabelStyle descStyle = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.BLACK);

        Table header = new Table();
        Label titleLabel = new Label(quest.getTitle(), descStyle);
        titleLabel.setFontScale(1.5f);
        header.add(titleLabel).left().expandX();
        header.add(priorityBadge(quest.getPriority())).right();
        card.add(header).fillX().row();

        Label descLabel = new Label(quest.getDescription(), descStyle);
        descLabel.setFontScale(1.15f);
        descLabel.setColor(Color.valueOf("444444"));
        descLabel.setWrap(true);
        card.add(descLabel).width(CARD_WIDTH - 100).left().padTop(6).row();

        Table progressRow = new Table();
        progressRow.add(buildProgressBar(quest)).size(BAR_WIDTH, BAR_HEIGHT).left();
        Label progressLabel = new Label(quest.getProgress().toString(), descStyle);
        progressLabel.setFontScale(1.1f);
        progressRow.add(progressLabel).padLeft(10);
        card.add(progressRow).left().padTop(10).row();

        Table footer = new Table();
        Label rewardLabel = new Label("Reward: " + quest.getRewardDescription(), descStyle);
        rewardLabel.setFontScale(1.05f);
        rewardLabel.setColor(Color.valueOf("1b5e20"));
        footer.add(rewardLabel).left().expandX();
        footer.add(statusWidget(quest)).right();
        card.add(footer).fillX().padTop(10).row();

        return card;
    }

    private Label priorityBadge(QuestPriority priority) {
        Color color = switch (priority) {
            case CRITICAL -> Color.SCARLET;
            case HIGH -> Color.ORANGE;
            case MEDIUM -> Color.GOLD;
            case LOW -> Color.LIGHT_GRAY;
        };
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), color);
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
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.DARK_GRAY);

        if (!quest.isCompleted()) {
            Label label = new Label("In Progress", style);
            label.setFontScale(1.05f);
            return label;
        }
        if (!quest.isClaimed()) {
            TextButton claimBtn = new TextButton("Collect", skin, "green_small");
            claimBtn.getLabel().setFontScale(1.1f);
            claimBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    controller.claimQuest(quest.getId());
                    refreshWallet();
                    refreshList();
                }
            });
            return claimBtn;
        }
        Label claimed = new Label("Claimed", style);
        claimed.setFontScale(1.05f);
        claimed.setColor(Color.valueOf("1b5e20"));
        return claimed;
    }

    private void buildMinigamesList() {
        for (int i = 0; i < MINI_GAME_SEASON_FOLDERS.length; i++) {
            listTable.add(buildMinigameCard(MINI_GAME_SEASON_FOLDERS[i], MINI_GAME_LABELS[i]))
                .width(CARD_WIDTH).height(150).padBottom(14).row();
        }
    }

    private Table buildMinigameCard(String seasonFolder, String displayName) {
        BorderedTable card = new BorderedTable();
        card.pad(10).padLeft(40).padRight(40);
        Label.LabelStyle descStyle = new Label.LabelStyle(skin.getFont("FBUSV8C5EI_1"), Color.BLACK);

        Table info = new Table();
        Label nameLabel = new Label(displayName, descStyle);
        nameLabel.setFontScale(1.5f);
        info.add(nameLabel).left().row();
        Label subLabel = new Label("Choose a level to play.", descStyle);
        subLabel.setFontScale(1.05f);
        subLabel.setColor(Color.valueOf("555555"));
        info.add(subLabel).left().padTop(6).row();
        card.add(info).expandX().left();

        Table levels = new Table();
        for (int level = 1; level <= 3; level++) {
            int chosenLevel = level;
            TextButton levelBtn = new TextButton("Level " + level, skin, "brown");
            levelBtn.getLabel().setFontScale(1.0f);
            levelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    game.setScreen(new GameScreen(seasonFolder, chosenLevel));
                }
            });
            levels.add(levelBtn).size(140, 55).padLeft(6);
        }
        card.add(levels).right();
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
        stage.dispose();
        skin.dispose();
        barBgTexture.dispose();
        barFillTexture.dispose();
    }
}
