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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.MatchSession;
import com.pvz.network.MatchDTOs;
import com.pvz.network.MessageType;
import com.pvz.network.NetworkClient;
import com.pvz.network.PlayerRole;
import com.pvz.view.game.GameScreen;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Opponent-selection screen for online I,Zombie, shown instead of jumping
 * straight into {@link GameScreen} the way the other minigames do.
 *
 * Flow:
 *  - "Play vs friend": type a username + pick your role, send an invite,
 *    wait for accept/reject.
 *  - "Play random": join the server's random queue, wait to be paired.
 *  - Either way, once the server pushes MATCH_FOUND, the match info is
 *    stashed on {@link AppContext#setMatchSession} and we hand off to
 *    GameScreen exactly like the single-player minigames do.
 *
 * Known limitation: incoming-invite popups only show up while this screen
 * is open. If the invited user is elsewhere in the app (MainMenu, shop,
 * ...), the invite waits on the server but they won't see a popup until
 * they open this screen themselves. A fully global popup would need a
 * persistent overlay Stage layered above every screen — not done here.
 */
public class OpponentSelectMenu extends ScreenAdapter {

    private static final float PANEL_WIDTH = 900f;
    private static final float PANEL_HEIGHT = 620f;

    private final PvZ2 game;
    private final String seasonFolder;
    private final int levelNumber;

    private Stage stage;
    private Skin skin;
    private Stack rootStack;
    private BorderedTable mainPanel;
    private Table contentTable;

    /** Non-null while we're waiting on our own invite's response. */
    private String pendingMatchId;
    /** True while we're sitting in the random queue. */
    private boolean inRandomQueue;

    public OpponentSelectMenu(PvZ2 game, String seasonFolder, int levelNumber) {
        this.game = game;
        this.seasonFolder = seasonFolder;
        this.levelNumber = levelNumber;
    }

    @Override
    public void show() {
        super.show();
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        MenuUiKit.installClickSound(stage);
        skin = PvzSkin.get();

        rootStack = new Stack();
        rootStack.setFillParent(true);
        stage.addActor(rootStack);

        MenuUiKit.installRotatingBackground(rootStack, game.getGlobalAssetManager());

        mainPanel = new BorderedTable();
        mainPanel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        mainPanel.setPosition((1920 - PANEL_WIDTH) / 2f, (1080 - PANEL_HEIGHT) / 2f);
        rootStack.add(mainPanel);

        mainPanel.add(buildTopBar()).fillX().padTop(15).padLeft(25).padRight(25).row();

        contentTable = new Table();
        mainPanel.add(contentTable).expand().fill().pad(20).row();

        registerPushListeners();
        showSelectState();
    }

    // --- Push listeners (server-initiated events) --------------------------------------

    private void registerPushListeners() {
        NetworkClient.getInstance().onInviteIncoming(this::handleIncomingInvite);

        NetworkClient.getInstance().onInviteRejected(payload -> {
            if (payload.matchId.equals(pendingMatchId)) {
                pendingMatchId = null;
                showSelectState();
                setStatus(payload.byUsername + " declined your invite.", Color.FIREBRICK);
            }
        });

        NetworkClient.getInstance().onMatchFound(payload -> {
            pendingMatchId = null;
            inRandomQueue = false;
            AppContext.getInstance().setMatchSession(
                new MatchSession(payload.matchId, payload.opponentUsername, payload.yourRole));
            game.setScreen(new GameScreen(seasonFolder, levelNumber));
        });
    }

    private void handleIncomingInvite(MatchDTOs.InviteIncomingPayload invite) {
        Table blocker = new Table();
        blocker.setFillParent(true);
        blocker.setBackground(solidDrawable(new Color(0f, 0f, 0f, 0.55f)));
        rootStack.add(blocker);

        BorderedTable popup = new BorderedTable();
        popup.pad(30);

        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.BLACK);
        Label message = new Label(invite.fromUsername + " wants to play I, Zombie with you!\nYou would play as: "
            + invite.yourRole, style);
        message.setFontScale(1.1f);
        message.setAlignment(com.badlogic.gdx.utils.Align.center);
        message.setWrap(true);
        popup.add(message).width(500).padBottom(25).row();

        Table buttons = new Table();
        TextButton accept = new TextButton("Accept", skin, "green_small");
        accept.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                rootStack.removeActor(blocker);
                NetworkClient.getInstance().respondToInvite(invite.matchId, true, null);
            }
        });
        TextButton reject = new TextButton("Decline", skin, "brown");
        reject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                rootStack.removeActor(blocker);
                NetworkClient.getInstance().respondToInvite(invite.matchId, false, null);
            }
        });
        buttons.add(accept).size(160, 55).padRight(15);
        buttons.add(reject).size(160, 55);
        popup.add(buttons);

        blocker.add(popup);
    }

    // --- Screen states -----------------------------------------------------------------

    private void showSelectState() {
        contentTable.clearChildren();

        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.BLACK);

        // -- Play vs a specific friend --
        Label friendHeader = new Label("Play vs a friend", labelStyle);
        friendHeader.setFontScale(1.3f);
        contentTable.add(friendHeader).left().padBottom(10).row();

        TextField usernameField = new TextField("", skin);
        usernameField.setMessageText("Friend's username");
        contentTable.add(usernameField).width(400).height(55).left().padBottom(12).row();

        Table roleRow = new Table();
        Label roleLabel = new Label("Play as:", labelStyle);
        roleLabel.setFontScale(1.05f);
        roleRow.add(roleLabel).padRight(10);

        final PlayerRole[] chosenRole = { PlayerRole.PLANT };
        TextButton plantBtn = new TextButton("Plants", skin, "purple");
        TextButton zombieBtn = new TextButton("Zombies", skin, "brown");
        plantBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                chosenRole[0] = PlayerRole.PLANT;
                plantBtn.setStyle(skin.get("purple", TextButton.TextButtonStyle.class));
                zombieBtn.setStyle(skin.get("brown", TextButton.TextButtonStyle.class));
            }
        });
        zombieBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                chosenRole[0] = PlayerRole.ZOMBIE;
                zombieBtn.setStyle(skin.get("purple", TextButton.TextButtonStyle.class));
                plantBtn.setStyle(skin.get("brown", TextButton.TextButtonStyle.class));
            }
        });
        roleRow.add(plantBtn).size(120, 46).padRight(8);
        roleRow.add(zombieBtn).size(120, 46);
        contentTable.add(roleRow).left().padBottom(15).row();

        TextButton inviteBtn = new TextButton("Send Invite", skin, "green_small");
        inviteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                String target = usernameField.getText().trim();
                if (target.isEmpty()) {
                    setStatus("Enter a username first.", Color.FIREBRICK);
                    return;
                }
                setStatus("Sending invite...", Color.DARK_GRAY);
                NetworkClient.getInstance().sendInvite(target, chosenRole[0], response -> {
                    if (!response.success) {
                        setStatus(response.errorMessage, Color.FIREBRICK);
                        return;
                    }
                    pendingMatchId = NetworkClient.getInstance().parsePayload(response, String.class);
                    showWaitingState("Waiting for " + target + " to respond...", () -> {
                        pendingMatchId = null;
                        showSelectState();
                    });
                });
            }
        });
        contentTable.add(inviteBtn).size(220, 55).left().padBottom(30).row();

        // -- Play random --
        Label randomHeader = new Label("Or play a random opponent", labelStyle);
        randomHeader.setFontScale(1.3f);
        contentTable.add(randomHeader).left().padBottom(10).row();

        TextButton randomBtn = new TextButton("Find Random Opponent", skin, "purple");
        randomBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                inRandomQueue = true;
                NetworkClient.getInstance().joinRandomQueue(response -> {
                    if (!response.success) {
                        inRandomQueue = false;
                        setStatus(response.errorMessage, Color.FIREBRICK);
                        return;
                    }
                    showWaitingState("Looking for an opponent...", () -> {
                        inRandomQueue = false;
                        NetworkClient.getInstance().cancelRandomQueue(null);
                        showSelectState();
                    });
                });
            }
        });
        contentTable.add(randomBtn).size(320, 55).left().padBottom(20).row();

        statusLabelRow();
    }

    private Label statusLabel;

    private void statusLabelRow() {
        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.DARK_GRAY);
        statusLabel = new Label("", style);
        statusLabel.setFontScale(1.0f);
        contentTable.add(statusLabel).left().row();
    }

    private void setStatus(String text, Color color) {
        if (statusLabel == null) return;
        statusLabel.setText(text);
        statusLabel.setColor(color);
    }

    private void showWaitingState(String message, Runnable onCancel) {
        contentTable.clearChildren();

        Label.LabelStyle style = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.BLACK);
        Label waitingLabel = new Label(message, style);
        waitingLabel.setFontScale(1.2f);
        contentTable.add(waitingLabel).padBottom(30).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                onCancel.run();
            }
        });
        contentTable.add(cancelBtn).size(180, 55);
    }

    // --- Top bar -------------------------------------------------------------------------

    private Table buildTopBar() {
        Table top = new Table();

        ImageButton backBtn = new ImageButton(
            MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (inRandomQueue) {
                    NetworkClient.getInstance().cancelRandomQueue(null);
                }
                game.setScreen(new TravelLogMenu(game));
            }
        });

        Label titleLabel = new Label("I, ZOMBIE — CHOOSE OPPONENT", skin, "big");
        titleLabel.setFontScale(1.2f);
        titleLabel.setColor(Color.BLACK);

        top.add(backBtn).size(75, 70).left();
        top.add(titleLabel).expandX().center();
        return top;
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable solidDrawable(Color color) {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
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
    public void hide() {
        // Stop reacting to invites/matches meant for whichever screen replaced this one.
        NetworkClient.getInstance().clearPushListener(MessageType.INVITE_INCOMING);
        NetworkClient.getInstance().clearPushListener(MessageType.INVITE_REJECTED);
        NetworkClient.getInstance().clearPushListener(MessageType.MATCH_FOUND);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
