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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.LeaderBoardController;
import com.pvz.models.AppContext;
import com.pvz.models.leaderboard.Leaderboard.LeaderBoardEntry;
import com.pvz.models.leaderboard.LeaderboardSortField;
import com.pvz.models.leaderboard.SortTypes;
import com.pvz.models.user.User;
import com.pvz.utils.AvatarImages;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Leaderboard screen: every saved player ranked and sortable by any column,
 * with medal ranks for the top 3 and the current user's row highlighted.
 * Backed by {@link LeaderBoardController}, which reads through the existing
 * Leaderboard model.
 *
 * Layout note: the header row and the scroll pane are both pinned to the
 * exact same ROW_WIDTH so they get identical auto-centered margins inside
 * the outer BorderedTable panel. That margin (40px each side) is what
 * clears BorderedTable's own decorative border texture - narrower than
 * that and the leftmost column (rank badges) render underneath the frame.
 */
public class LeaderboardMenu extends ScreenAdapter {

    private static final String AVATAR_DIR = "textures/avatars/";
    private static final String DEFAULT_AVATAR = AVATAR_DIR + "avatar_luffy.png";

    private static final float PANEL_WIDTH = 1360f;
    private static final float PANEL_HEIGHT = 830f;
    private static final float ROW_WIDTH = 1300f;

    private static final float COL_RANK = 65f;
    private static final float COL_AVATAR = 65f;
    private static final float COL_NAME = 153f;
    private static final float COL_PROGRESS = 143f;
    private static final float COL_MINIGAMES = 99f;
    private static final float COL_DAILY = 105f;
    private static final float COL_NONDAILY = 105f;
    private static final float COL_SCORE = 93f;
    private static final float COL_MIOPOINT = 93f;

    private final PvZ2 game;
    private final LeaderBoardController controller;

    private Stage stage;
    private Skin skin;
    private BorderedTable mainPanel;
    private Table sortBar;
    private Table listTable;
    private Label coinsLabel;
    private Label diamondsLabel;
    private Texture darkBgTexture;

    public LeaderboardMenu(PvZ2 game) {
        this.game = game;
        this.controller = new LeaderBoardController();
    }

    @Override
    public void show() {
        super.show();
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        MenuUiKit.installClickSound(stage);
        skin = PvzSkin.get();

        MenuUiKit.installRotatingBackground(stage.getRoot(), game.getGlobalAssetManager());

        mainPanel = new BorderedTable();
        mainPanel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        mainPanel.setPosition((1920 - PANEL_WIDTH) / 2f, (1080 - PANEL_HEIGHT) / 2f);
        stage.addActor(mainPanel);

        darkBgTexture = roundedRectTexture(1326, 798, 16, new Color(0.24f, 0.14f, 0.06f, 1f));
        Image darkBg = new Image(darkBgTexture);
        darkBg.setBounds(17, 16, 1326, 798);
        mainPanel.addActor(darkBg);

        mainPanel.add(buildTopBar()).fillX().padTop(15).padLeft(25).padRight(25).row();

        sortBar = new Table();
        mainPanel.add(sortBar).padTop(15).row();

        mainPanel.add(buildColumnHeader()).width(ROW_WIDTH).padTop(14).row();

        listTable = new Table();
        listTable.top();
        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        // Overlay the scrollbar instead of reserving extra width for it, so the
        // rows stay pinned to exactly ROW_WIDTH - same as the header above.
        scrollPane.setScrollbarsOnTop(true);
        scrollPane.setOverscroll(false, false);
        mainPanel.add(scrollPane).width(ROW_WIDTH).height(560).padTop(10).padBottom(15).row();

        buildSortBar();
        refreshWallet();
        refreshRows(); // اول با کش خالی نمایش می‌ده (empty state)
        controller.reload(this::refreshRows); // وقتی سرور جواب داد، جدول دوباره ساخته می‌شه
    }

    // --- Top bar --------------------------------------------------------

    private Table buildTopBar() {
        Table top = new Table();

        ImageButton backBtn = new ImageButton(
                MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GameModesMenu(game));
            }
        });

        Label titleLabel = new Label("LEADERBOARD", skin, "big");
        titleLabel.setFontScale(1.4f);
        titleLabel.setColor(Color.valueOf("F1E4C0"));

        top.add(backBtn).size(75, 70).left();
        top.add(titleLabel).expandX().center();
        top.add(buildWallet()).right();
        return top;
    }

    private Table buildWallet() {
        Table table = new Table();
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.valueOf("F1E4C0"));

        Image coinIcon = new Image(skin.getDrawable("image_ui_generic_coin_icon_small"));
        coinsLabel = new Label("0", style);
        coinsLabel.setFontScale(1.2f);

        Image gemIcon = new Image(skin.getDrawable("image_ui_generic_gem_icon_small"));
        diamondsLabel = new Label("0", style);
        diamondsLabel.setFontScale(1.2f);

        table.add(coinIcon).size(32, 32);
        table.add(coinsLabel).padLeft(6).padRight(25);
        table.add(gemIcon).size(32, 42);
        table.add(diamondsLabel).padLeft(6);
        return table;
    }

    private void refreshWallet() {
        User user = AppContext.getInstance().getCurrentUser();
        coinsLabel.setText(user == null ? "0" : String.valueOf(user.getProfile().getCoins()));
        diamondsLabel.setText(user == null ? "0" : String.valueOf(user.getProfile().getDiamonds()));
    }

    // --- Sort bar ---------------------------------------------------------

    private void buildSortBar() {
        sortBar.clearChildren();
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.valueOf("F1E4C0"));
        Label sortByLabel = new Label("Sort by:", style);
        sortByLabel.setFontScale(1.05f);
        sortBar.add(sortByLabel).padRight(10);

        for (LeaderboardSortField field : LeaderboardSortField.values()) {
            sortBar.add(sortChip(field)).width(150).height(46).padLeft(4).padRight(4);
        }
        sortBar.add(orderToggle()).width(100).height(46).padLeft(20);
    }

    private TextButton sortChip(LeaderboardSortField field) {
        String style = field == controller.getSortField() ? "purple" : "brown";
        TextButton button = new TextButton(chipLabel(field), skin, style);
        button.getLabel().setFontScale(1.0f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                controller.setSortField(field);
                buildSortBar();
                refreshRows();
            }
        });
        return button;
    }

    private String chipLabel(LeaderboardSortField field) {
        return switch (field) {
            case LAST_LEVEL_AND_SEASON -> "Progress";
            case MINI_GAMES_PASSED -> "Minigames";
            case DAILY_QUESTS_COMPLETED -> "Daily";
            case NON_DAILY_QUESTS_COMPLETED -> "Quests";
            case HIGHEST_SCORING_GAME_SCORE -> "Score";
            case BEST_MIOPOINT -> "Miopoint";
        };
    }

    private TextButton orderToggle() {
        String label = controller.getSortOrder() == SortTypes.DESCENDING ? "Desc" : "Asc";
        TextButton button = new TextButton(label, skin, "green_small");
        button.getLabel().setFontScale(1.0f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                controller.toggleSortOrder();
                buildSortBar();
                refreshRows();
            }
        });
        return button;
    }

    // --- Column header ------------------------------------------------

    private Table buildColumnHeader() {
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.valueOf("D8C9A8"));
        Table header = new Table();
        header.pad(0, 16, 4, 16);
        header.defaults().left().padRight(8);

        header.add(headerLabel("#", style)).width(COL_RANK).center();
        header.add(headerLabel("", style)).width(COL_AVATAR);
        header.add(headerLabel("Player", style)).width(COL_NAME);
        header.add(headerLabel("Progress", style)).width(COL_PROGRESS);
        header.add(headerLabel("Minigames", style)).width(COL_MINIGAMES);
        header.add(headerLabel("Daily", style)).width(COL_DAILY);
        header.add(headerLabel("Quests", style)).width(COL_NONDAILY);
        header.add(headerLabel("Score", style)).width(COL_SCORE);
        header.add(headerLabel("Miopoint", style)).width(COL_MIOPOINT);
        return header;
    }

    private Label headerLabel(String text, Label.LabelStyle style) {
        Label label = new Label(text, style);
        label.setFontScale(1.05f);
        return label;
    }

    // --- Rows -----------------------------------------------------------

    private void refreshRows() {
        listTable.clearChildren();

        List<LeaderBoardEntry> entries = controller.getEntries();
        if (entries.isEmpty()) {
            Label empty = new Label("No players on the leaderboard yet.", skin);
            empty.setColor(Color.DARK_GRAY);
            listTable.add(empty).pad(30).row();
            return;
        }

        String currentUsername = controller.getCurrentUsername();
        int rank = 1;
        for (LeaderBoardEntry entry : entries) {
            boolean isYou = currentUsername != null && currentUsername.equalsIgnoreCase(entry.username);
            listTable.add(buildRow(rank++, entry, isYou)).width(ROW_WIDTH).height(84).padBottom(10).row();
        }
    }

    private Table buildRow(int rank, LeaderBoardEntry entry, boolean isYou) {
        Table row = new Table();
        row.setBackground(isYou ? highlightRowDrawable() : flatRowDrawable());
        row.pad(8, 16, 8, 16);
        row.defaults().left().padRight(8);

        Label.LabelStyle mainStyle = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"),
                Color.valueOf("F1E4C0"));

        row.add(rankBadge(rank, mainStyle)).width(COL_RANK).center();
        row.add(avatarImage(entry.username)).size(COL_AVATAR - 10);
        row.add(nameLabel(entry.username, mainStyle)).width(COL_NAME);
        row.add(bodyLabel(progressText(entry), mainStyle)).width(COL_PROGRESS);
        row.add(bodyLabel(String.valueOf(entry.miniGamesPassed), mainStyle)).width(COL_MINIGAMES);
        row.add(bodyLabel(String.valueOf(entry.dailyQuestsCompleted), mainStyle)).width(COL_DAILY);
        row.add(bodyLabel(String.valueOf(entry.nonDailyQuestsCompleted), mainStyle)).width(COL_NONDAILY);
        row.add(scoreLabel(entry.highestScore)).width(COL_SCORE);
        row.add(scoreLabel(entry.bestMiopoint)).width(COL_MIOPOINT);

        return row;
    }

    private String progressText(LeaderBoardEntry entry) {
        if (entry.lastLevel <= 0) {
            return "—";
        }
        return "Ch " + entry.lastSeason + " • Lvl " + entry.lastLevel;
    }

    private Label nameLabel(String username, Label.LabelStyle style) {
        Label label = new Label(username, style);
        label.setFontScale(1.3f);
        label.setEllipsis(true);
        return label;
    }

    private Label bodyLabel(String text, Label.LabelStyle style) {
        Label label = new Label(text, style);
        label.setFontScale(1.15f);
        return label;
    }

    private Label scoreLabel(int score) {
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.valueOf("F1E4C0"));
        Label label = new Label(String.valueOf(score), style);
        label.setFontScale(1.3f);
        return label;
    }

    private Actor avatarImage(String username) {
        String path = DEFAULT_AVATAR;
        User user = AppContext.getInstance().getCurrentUser();
        if (user != null && username.equalsIgnoreCase(user.getUsername()) && user.getProfilePicture() != null
                && !user.getProfilePicture().isEmpty()) {
            path = user.getProfilePicture();
        }
        Texture feathered = AvatarImages.featheredCircle(path);
        return new Image(new TextureRegionDrawable(feathered));
    }

    private Actor rankBadge(int rank, Label.LabelStyle fallbackStyle) {
        Color medalColor = switch (rank) {
            case 1 -> new Color(1f, 0.84f, 0f, 1f);
            case 2 -> new Color(0.80f, 0.80f, 0.82f, 1f);
            case 3 -> new Color(0.80f, 0.50f, 0.20f, 1f);
            default -> null;
        };

        Stack badge = new Stack();
        if (medalColor != null) {
            Table circleWrap = new Table();
            circleWrap.add(new Image(circleDrawable(medalColor, 44))).size(44, 44);
            badge.add(circleWrap);
        }

        Label.LabelStyle style = medalColor != null
                ? new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.valueOf("F1E4C0"))
                : fallbackStyle;
        Label number = new Label(String.valueOf(rank), style);
        number.setFontScale(1.05f);
        number.setAlignment(com.badlogic.gdx.utils.Align.center);
        Table numberWrap = new Table();
        numberWrap.add(number).center();
        badge.add(numberWrap);

        // Fixed-size container so ranks with a medal circle and plain-number
        // ranks (4+) occupy the exact same footprint and line up in a column.
        Table container = new Table();
        container.add(badge).size(44, 44);
        return container;
    }

    private Drawable circleDrawable(Color color, int diameter) {
        Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(color);
        pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2);
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    private Drawable flatRowDrawable() {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.10f));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    private Drawable highlightRowDrawable() {
        int width = 32;
        int height = 32;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.55f, 0.42f, 0.08f, 0.55f));
        pixmap.fill();
        pixmap.setColor(new Color(1f, 0.84f, 0.2f, 0.95f));
        pixmap.drawRectangle(0, 0, width, height);
        pixmap.drawRectangle(1, 1, width - 2, height - 2);
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
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
        if (darkBgTexture != null)
            darkBgTexture.dispose();
    }
}
