package com.pvz.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
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

import pvz.skin.PvzSkin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

    private static final Map<String, Texture> ROUNDED_CACHE = new HashMap<>();

    private static int cornerRadius(float cardW, float cardH) {
        return Math.min(Math.min((int) cardW, (int) cardH) / 8, 24);
    }

    /**
     * Rounded-corner card texture: art stretched to the card size with the bottom shadow
     * band baked in. Loaded via {@link Pixmap} directly so the corners can be masked —
     * {@code new Texture(FileHandle)} disposes its source pixmap before we could touch it.
     */
    private static Texture roundedCardTexture(String artPath, float cardW, float cardH) {
        int w = Math.max(1, (int) cardW);
        int h = Math.max(1, (int) cardH);
        String key = artPath + "@" + w + "x" + h;
        Texture cached = ROUNDED_CACHE.get(key);
        if (cached != null) return cached;

        Pixmap src = new Pixmap(Gdx.files.internal(artPath));
        Pixmap out = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        scaleDraw(src, out);
        src.dispose();

        bakeShadow(out, w, h);
        roundCorners(out, cornerRadius(cardW, cardH));

        Texture texture = new Texture(out);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        out.dispose();
        ROUNDED_CACHE.put(key, texture);
        return texture;
    }

    /** Scales {@code src} into {@code dst} (nearest-neighbor) — avoids Pixmap.drawPixmap scaling API drift. */
    private static void scaleDraw(Pixmap src, Pixmap dst) {
        int sw = src.getWidth(), sh = src.getHeight();
        int dw = dst.getWidth(), dh = dst.getHeight();
        dst.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < dh; y++) {
            int sy = Math.min(sh - 1, (y * sh) / dh);
            for (int x = 0; x < dw; x++) {
                int sx = Math.min(sw - 1, (x * sw) / dw);
                dst.drawPixel(x, y, src.getPixel(sx, sy));
            }
        }
    }

    /** Rounded-corner solid, used for the missing-art tinted panel and the locked overlay. */
    private static Texture roundedSolidTexture(Color color, float cardW, float cardH) {
        int w = Math.max(1, (int) cardW);
        int h = Math.max(1, (int) cardH);
        String key = "solid@" + color.toIntBits() + "@" + w + "x" + h;
        Texture cached = ROUNDED_CACHE.get(key);
        if (cached != null) return cached;

        Pixmap out = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        out.setColor(color);
        out.fill();
        bakeShadow(out, w, h);
        roundCorners(out, cornerRadius(cardW, cardH));

        Texture texture = new Texture(out);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        out.dispose();
        ROUNDED_CACHE.put(key, texture);
        return texture;
    }

    /** Soft dark band across the bottom so the title/caption stays readable. */
    private static void bakeShadow(Pixmap pix, int w, int h) {
        int shadowH = Math.min(64, h / 3);
        pix.setColor(0f, 0f, 0f, 0.55f);
        pix.setBlending(Pixmap.Blending.SourceOver);
        pix.fillRectangle(0, h - shadowH, w, shadowH);
    }

    /** Sets the alpha of pixels outside the rounded-rect corners to zero. */
    private static void roundCorners(Pixmap pix, int radius) {
        int w = pix.getWidth(), h = pix.getHeight();
        int r = Math.min(radius, Math.min(w, h) / 2);
        if (r <= 0) return;
        pix.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int dx = x < r ? r - x : (x >= w - r ? x - (w - 1 - r) : 0);
                int dy = y < r ? r - y : (y >= h - r ? y - (h - 1 - r) : 0);
                if (dx != 0 && dy != 0 && dx * dx + dy * dy > r * r) {
                    int px = pix.getPixel(x, y);
                    pix.setColor(((px >> 24) & 0xFF) / 255f, ((px >> 16) & 0xFF) / 255f,
                        ((px >> 8) & 0xFF) / 255f, 0f);
                    pix.drawPixel(x, y);
                }
            }
        }
    }

    /**
     * A big card with rounded-corner background art, a soft bottom shadow band, a title and an
     * optional status caption (e.g. "Locked" / "Unlocked"). Used for the mode/chapter grids.
     */
    public static Actor bigCard(Skin skin, String artPath, Color accent, String title,
                                String caption, boolean locked, Runnable onClick,
                                float cardW, float cardH) {
        Stack stack = new Stack();

        boolean hasArt = artPath != null && !artPath.isEmpty() && Gdx.files.internal(artPath).exists();
        if (hasArt) {
            stack.add(new Image(roundedCardTexture(artPath, cardW, cardH)));
        } else {
            // no art dropped in yet -> show a tinted panel instead of an empty card.
            // Logged so you can see in the console exactly which path it looked for.
            if (artPath != null && !artPath.isEmpty()) {
                Gdx.app.log("MenuUiKit", "card art not found, looked at internal path: \"" + artPath
                    + "\" (resolved to " + Gdx.files.internal(artPath).file().getAbsolutePath() + ")");
            }
            stack.add(new Image(roundedSolidTexture(accent, cardW, cardH)));
        }

        Table textTable = new Table();
        textTable.bottom();
        Label titleLabel = new Label(title, skin);
        titleLabel.setFontScale(1.05f);
        textTable.add(titleLabel).padBottom(caption != null ? 22 : 10).row();
        if (caption != null && !caption.isEmpty()) {
            Label captionLabel = new Label(caption, skin);
            captionLabel.setFontScale(0.85f);
            captionLabel.setColor(locked ? new Color(1f, 0.6f, 0.6f, 1f) : new Color(0.65f, 1f, 0.7f, 1f));
            textTable.add(captionLabel).padBottom(8);
        }
        stack.add(textTable);

        if (locked) {
            stack.add(new Image(roundedSolidTexture(new Color(0f, 0f, 0f, 0.45f), cardW, cardH)));
        }

        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!locked && onClick != null) onClick.run();
            }
        });

        return stack;
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
        viewport.add(pane).width(viewportWidth).height(cardHeight);

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
