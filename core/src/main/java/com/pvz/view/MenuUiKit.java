package com.pvz.view;

import java.util.List;
import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.controller.AudioManager;
import com.pvz.models.AppContext;
import com.pvz.models.user.Profile;
import com.pvz.models.user.User;

import pvz.skin.PvzSkin;

/**
 * Small shared helper used by MainMenu, GameModesMenu and AdventureMenu to build the
 * "hub" look (top resource bar + big tappable cards) instead of plain buttons on a void.
 * <p>
 * Every method here is defensive about missing art: if the texture path you pass in
 * doesn't exist yet on disk, it falls back to a soft colored panel instead of crashing,
 * so you (or ) can drop the real PNGs in later without touching this file again.
 */
public final class MenuUiKit {

    private MenuUiKit() {
    }

    // ---------------------------------------------------------------- textures

    /** Loads a texture from assets, or returns a small solid-color fallback if it's missing. */
    public static Texture loadTextureSafe(String path) {
        if (path != null && !path.isEmpty() && Gdx.files.internal(path).exists()) {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return texture;
        }
        return solidTexture(new Color(0f, 0f, 0f, 0f));
    }

    /** Builds a tiny solid-color texture, handy as a placeholder background/panel. */
    public static Texture solidTexture(Color color) {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static Drawable solidDrawable(Color color) {
        return new TextureRegionDrawable(solidTexture(color));
    }

    /** Installs a rotating background that crossfades between bg1–bg4 into the given parent group. */
    public static void installRotatingBackground(Group parent, com.badlogic.gdx.assets.AssetManager assets) {
        RotatingBackground.install(parent, assets);
    }

    /**
     * Plays the shared UI click sound on every touch on this stage. InputListener
     * events bubble up from the pressed actor to the stage root, so a single
     * listener here covers all buttons/cards without touching each handler.
     */
    public static void installClickSound(Stage stage) {
        stage.getRoot().addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                AudioManager.getInstance().playClick();
                return false;
            }
        });
    }

    /** How far (at the 130px reference size) the value number sits from the left edge of the
     *  coin/diamond pill. Lower = number sits further left, right after the icon. */
    private static final float RESOURCE_LABEL_PAD_FACTOR = 50f;

    // ---------------------------------------------------------------- top bar pieces

    /** Coin/diamond style pill: an icon on the left and a value label on top of it. */
    public static Table resourceWidget(Skin skin, String iconPath, Color fallbackTint, String value) {
        return resourceWidget(skin, iconPath, fallbackTint, value, 130, 42);
    }

    /** Coin/diamond style pill with a custom size. Label padding and font scale follow the width. */
    public static Table resourceWidget(Skin skin, String iconPath, Color fallbackTint, String value, float width, float height) {
        Stack stack = new Stack();

        Table bg = new Table();
        Texture iconTexture = loadTextureSafe(iconPath);
        if (Gdx.files.internal(iconPath) != null && Gdx.files.internal(iconPath).exists()) {
            bg.setBackground(new TextureRegionDrawable(iconTexture));
        } else {
            bg.setBackground(solidDrawable(new Color(0f, 0f, 0f, 0.45f)));
            Image dot = new Image(solidDrawable(fallbackTint));
            Table dotWrap = new Table();
            dotWrap.add(dot).size(20, 20).left().padLeft(6);
            stack.add(dotWrap);
        }
        stack.add(bg);

        Label label = new Label(value, skin);
        label.setFontScale(1.2f * (width / 130f));
        Table textTable = new Table();
        textTable.add(label).left().expand().padLeft(RESOURCE_LABEL_PAD_FACTOR * (width / 130f));
        stack.add(textTable);

        Table outer = new Table();
        outer.add(stack).size(width, height);
        return outer;
    }

    // ---------------------------------------------------------------- wallet "+" buttons

    /** Width of the "+" button, as a fraction of the pill width. */
    private static final float PLUS_BUTTON_WIDTH_FACTOR = 0.24f;
    /** Height of the "+" button, as a fraction of the pill height. */
    private static final float PLUS_BUTTON_HEIGHT_FACTOR = 0.72f;
    /** Gap between the "+" button and the pill's right edge, as a fraction of the pill width. */
    private static final float PLUS_BUTTON_PAD_RIGHT_FACTOR = -0.48f;

    /** A coin/diamond pill whose engraved "+" is a real button. Exposes the value {@link #label} and the
     *  {@link #plusButton} so callers can refresh the number and wire the cheat. */
    public static class PlusResourceWidget {
        /** The pill table, add this into your top bar. */
        public final Table widget;
        /** The coin/diamond value label, so callers can refresh it after a click. */
        public final Label label;
        /** The "+" button overlay (the fake plus turned real). */
        public final ImageButton plusButton;

        PlusResourceWidget(Table widget, Label label, ImageButton plusButton) {
            this.widget = widget;
            this.label = label;
            this.plusButton = plusButton;
        }
    }

    /** Same pill look as {@link #resourceWidget}, but with a working "+" button over the engraved one. */
    public static PlusResourceWidget resourceWidgetWithPlus(Skin skin, String iconPath, Color fallbackTint, String value,
                                                           float width, float height) {
        Stack stack = new Stack();

        Table bg = new Table();
        Texture iconTexture = loadTextureSafe(iconPath);
        if (Gdx.files.internal(iconPath) != null && Gdx.files.internal(iconPath).exists()) {
            bg.setBackground(new TextureRegionDrawable(iconTexture));
        } else {
            bg.setBackground(solidDrawable(new Color(0f, 0f, 0f, 0.45f)));
            Image dot = new Image(solidDrawable(fallbackTint));
            Table dotWrap = new Table();
            dotWrap.add(dot).size(20, 20).left().padLeft(6);
            stack.add(dotWrap);
        }
        stack.add(bg);

        Label label = new Label(value, skin);
        label.setFontScale(1.2f * (width / 130f));
        Table textTable = new Table();
        textTable.add(label).left().expand().padLeft(RESOURCE_LABEL_PAD_FACTOR * (width / 130f));
        stack.add(textTable);

        ImageButton plusButton = new ImageButton(plusButtonStyle(skin));
        Table plusWrap = new Table();
        plusWrap.add(plusButton).size(width * PLUS_BUTTON_WIDTH_FACTOR, height * PLUS_BUTTON_HEIGHT_FACTOR)
            .right().padRight(width * PLUS_BUTTON_PAD_RIGHT_FACTOR);
        stack.add(plusWrap);

        Table outer = new Table();
        outer.add(stack).size(width, height);
        return new PlusResourceWidget(outer, label, plusButton);
    }

    /** "+" button look: the same coin-buy asset the in-game HUD uses for its cheat buttons. */
    private static ImageButton.ImageButtonStyle plusButtonStyle(Skin skin) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = skin.getDrawable("image_ui_hud_ingame_coin_buy");
        style.imageDown = skin.getDrawable("image_ui_hud_ingame_coin_buy_down");
        style.imageOver = skin.getDrawable("image_ui_hud_ingame_coin_buy_down");
        style.imageChecked = skin.getDrawable("image_ui_hud_ingame_coin_buy_down");
        return style;
    }

    /**
     * Turns the "+" of a wallet pill into a working debug cheat (coins +amount or gems +amount) and saves
     * the profile on every click. When debug mode is off the button is disabled and tinted out.
     */
    public static void wirePlusButton(PlusResourceWidget widget, boolean coins, int amount) {
        boolean debug = isDebugModeEnabled();
        widget.plusButton.setTouchable(debug ? Touchable.enabled : Touchable.disabled);
        if (!debug) widget.plusButton.setColor(0.5f, 0.5f, 0.5f, 0.6f);
        widget.plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isDebugModeEnabled()) return;
                User user = AppContext.getInstance().getCurrentUser();
                if (user == null || user.getProfile() == null) return;
                Profile profile = user.getProfile();
                if (coins) {
                    profile.addCoins(amount);
                } else {
                    profile.addDiamonds(amount);
                }
                user.saveUser();
                widget.label.setText(String.valueOf(coins ? profile.getCoins() : profile.getDiamonds()));
            }
        });
    }

    /** True only when a logged-in user exists and has the debug-mode setting on. */
    private static boolean isDebugModeEnabled() {
        User user = AppContext.getInstance().getCurrentUser();
        return user != null && user.getSetting() != null && user.getSetting().isDebugMode();
    }

    /** A round-ish icon button with a caption underneath (profile, settings, news...). */
    public static Actor iconButtonWithLabel(Skin skin, Drawable iconDrawable, float size, String caption, Runnable onClick) {
        Table container = new Table();
        ImageButton button = new ImageButton(iconDrawable);
        button.getImageCell().size(size, size);
        container.add(button).row();
        if (caption != null && !caption.isEmpty()) {
            Label label = new Label(caption, skin);
            label.setFontScale(0.8f);
            container.add(label).padTop(2);
        }
        container.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.run();
            }
        });
        return container;
    }

    // ---------------------------------------------------------------- big tappable cards

    /**
     * A big card with rounded-corner background art and the chapter name up top. Locked
     * chapter cards (showLock) get a gold lock badge; game-mode cards pass showLock=false.
     */
    /** Size of the gold lock badge (width, height) on locked chapter cards. */
    private static final float LOCK_BADGE_WIDTH = 104f;
    private static final float LOCK_BADGE_HEIGHT = 144f;

    /** Chapter-name label on big cards: font scale and distance from the card's top edge. */
    private static final float CARD_TITLE_FONT_SCALE = 2.3f;
    private static final float CARD_TITLE_PAD_TOP = -10f;

    public static Actor bigCard(Skin skin, String artPath, Color accent, String title,
                                boolean locked, boolean showLock, Runnable onClick,
                                float cardW, float cardH) {
        Stack stack = new Stack();

        boolean hasArt = artPath != null && !artPath.isEmpty() && Gdx.files.internal(artPath).exists();
        if (hasArt) {
            Image art = new Image(loadTextureSafe(artPath));
            art.setScaling(Scaling.fit); // natural aspect ratio, no stretch/crop
            stack.add(art);
        } else {
            // no art dropped in yet -> a plain flat-color placeholder, not a styled frame.
            // Logged so you can see in the console exactly which path it looked for.
            if (artPath != null && !artPath.isEmpty()) {
                Gdx.app.log("MenuUiKit", "card art not found, looked at internal path: \"" + artPath
                    + "\" (resolved to " + Gdx.files.internal(artPath).file().getAbsolutePath() + ")");
            }
            Image placeholder = new Image(solidTexture(accent));
            stack.add(placeholder);
        }

        Table textTable = new Table();
        textTable.top();
        Label titleLabel = new Label(title, skin);
        titleLabel.setFontScale(CARD_TITLE_FONT_SCALE);
        textTable.add(titleLabel).padTop(CARD_TITLE_PAD_TOP);
        stack.add(textTable);

        if (locked && showLock) {
            Table lockTable = new Table();
            float size = Math.min(cardW * 0.5f, Math.min(LOCK_BADGE_WIDTH, LOCK_BADGE_HEIGHT));
            lockTable.add(new Image(loadTextureSafe("textures/greenhouse/goldlock_icon.png"))).size(LOCK_BADGE_WIDTH, LOCK_BADGE_HEIGHT);
            stack.add(lockTable);
        }

        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!locked && onClick != null) onClick.run();
            }
        });

        return stack;
    }

    // ---------------------------------------------------------------- hover pop effect

    /**
     * Adds a smooth "pop up" hover effect: the card grows up from its base while the pointer
     * is over it and settles back when the pointer leaves. Pointer state is polled directly
     * (not via enter/exit events), so it works reliably inside the scrolling carousel.
     */
    /** How much a card grows on hover — 1.12 = 12% larger. Shared by every card
     *  that uses {@link #addHoverPop}, so chapters and game modes always match. */
    public static final float CARD_HOVER_SCALE = 1.12f;

    /**
     * Adds a smooth "pop up" hover effect: the card grows up from its base while the pointer
     * is over it and settles back when the pointer leaves. Driven by real Scene2D
     * enter/exit hover events (filtered to pointer == -1, i.e. actual mouse movement,
     * not touch/drag), not manual per-frame hit-testing.
     */
    public static Actor addHoverPop(final Actor actor) {
        final boolean[] hovering = {false};
        // Table/Stack disable transforms by default, so an explicit enable is required
        // for setScale to actually affect drawing.
        if (actor instanceof Group) ((Group) actor).setTransform(true);

        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) { // real mouse hover, not a touch/drag pointer
                    hovering[0] = true;
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    hovering[0] = false;
                }
            }
        });

        actor.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                actor.setOrigin(actor.getWidth() / 2f, 0f);
                float target = hovering[0] ? CARD_HOVER_SCALE : 1f;
                float next = actor.getScaleX() + (target - actor.getScaleX()) * Math.min(1f, delta * 10f);
                actor.setScale(next);
                return false;
            }
        });
        return actor;
    }

    // ---------------------------------------------------------------- scrolling card row

    /**
     * Builds a smooth, flick-scrollable, infinitely-looping row of cards — the same trick
     *  carousel uses: the row is laid out three times back to back, starts scrolled
     * to the middle copy, and quietly snaps back a lap whenever you scroll off either edge,
     * so it feels endless in both directions.
     * <p>
     * Pass one factory per card (not the card itself) because each of the 3 laps needs its
     * own Actor instance — a single Actor can't appear twice in the scene graph.
     */
    public static Carousel buildCarousel(List<Supplier<Actor>> cardFactories, float cardWidth,
                                         float cardHeight, float cardPad, float viewportWidth) {
        // extra vertical room so the hover "pop up" (scale from the card base) isn't clipped
        return buildCarousel(cardFactories, cardWidth, cardHeight, cardPad, viewportWidth, cardHeight * 1.2f);
    }

    /** Same as {@link #buildCarousel(List, float, float, float, float)} but with an explicit viewport height. */
    public static Carousel buildCarousel(List<Supplier<Actor>> cardFactories, float cardWidth,
                                         float cardHeight, float cardPad, float viewportWidth,
                                         float viewportHeight) {
        Table content = new Table();
        for (int lap = 0; lap < 3; lap++) {
            for (Supplier<Actor> factory : cardFactories) {
                content.add(factory.get()).size(cardWidth, cardHeight).pad(0, cardPad, 0, cardPad);
            }
        }

        ScrollPane pane = new ScrollPane(content);
        pane.setOverscroll(false, false);
        pane.setFlickScroll(true);
        pane.setScrollingDisabled(false, true);

        float unitWidth = (cardWidth + cardPad * 2) * cardFactories.size();

        Table viewport = new Table();
        viewport.add(pane).width(viewportWidth).height(viewportHeight);

        Carousel carousel = new Carousel(pane, viewport, unitWidth);
        Gdx.app.postRunnable(() -> pane.setScrollX(unitWidth));
        return carousel;
    }

    /** Holds the ScrollPane + the Table you actually add to your layout, plus the per-frame wrap logic. */
    public static final class Carousel {
        public final ScrollPane pane;
        public final Table viewport;
        private final float unitWidth;

        private Carousel(ScrollPane pane, Table viewport, float unitWidth) {
            this.pane = pane;
            this.viewport = viewport;
            this.unitWidth = unitWidth;
        }

        /** Call this once per frame (e.g. at the top of render()) to keep the loop seamless. */
        public void update() {
            float maxX = pane.getMaxX();
            float currentX = pane.getScrollX();
            if (currentX <= 0f) {
                pane.setScrollX(Math.min(currentX + unitWidth, maxX));
            } else if (currentX >= maxX) {
                pane.setScrollX(Math.max(currentX - unitWidth, 0f));
            }
        }
    }

    // ---------------------------------------------------------------- navigation

    /** Back-button texture path, loaded once by {@code PvZ2}. */
    public static final String BACK_BUTTON_TEX = "textures/ui/buttons_hud_back_normal.png";

    /** Builds a drawable from a raw texture (used for the PNG back button). */
    public static Drawable textureDrawable(Texture texture) {
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    /** Back button built from the shared PNG, meant to sit top-left next to the screen title. */
    public static Actor backButton(Drawable up, Runnable onClick) {
        ImageButton back = new ImageButton(up);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.run();
            }
        });
        return back;
    }

    /** Generic close (X) button, e.g. in a panel corner or modal header. */
    public static ImageButton closeButton(Runnable onClick) {
        ImageButton close = new ImageButton(PvzSkin.get(), "generic_close");
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.run();
            }
        });
        return close;
    }

    public static Container<Label> notificationBadge(Label label) {
        Container<Label> container = new Container<>(label);
        container.top().right();
        container.padTop(-12).padRight(-12);
        return container;
    }
}
