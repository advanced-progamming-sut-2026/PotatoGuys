package com.pvz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Json;
import com.pvz.controller.AudioManager;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.user.Setting;
import com.pvz.models.user.User;
import com.pvz.network.NetworkClient;
import com.pvz.server.PvzServer;
import com.pvz.utils.SaveManager;
import com.pvz.view.LoginMenu;
import com.pvz.view.MainMenu;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.io.File;
import java.io.IOException;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class PvZ2 extends Game {
    public static TextureBank textureBank;
    public static PamPlayer pamPlayer;
    public static SpriteBatch batch;
    public static PvZ2 instance;
    AssetManager globalAssetManager;

    // Screen-space overlay used to apply the Settings screen's Brightness slider
    // everywhere in the game, not just while the Settings screen itself is open.
    // Kept as its own tiny batch/texture so it can never interfere with anything
    // else on the screen's own Stage.
    private SpriteBatch brightnessBatch;
    private Texture brightnessPixel;
    private final Matrix4 brightnessProjection = new Matrix4();

    public PvZ2() {
        globalAssetManager = new AssetManager();
        globalAssetManager.load("textures/backgrounds/MainMenu.png", Texture.class);
        globalAssetManager.load("textures/backgrounds/bg1.png", Texture.class);
        globalAssetManager.load("textures/backgrounds/bg2.png", Texture.class);
        globalAssetManager.load("textures/backgrounds/bg3.png", Texture.class);
        globalAssetManager.load("textures/backgrounds/bg4.png", Texture.class);
        globalAssetManager.load("textures/pvz2_logo_horizontal.png", Texture.class);
        globalAssetManager.load("textures/ui/news_button.png", Texture.class);
        globalAssetManager.load("news_preview/news_selected2.png", Texture.class);
        globalAssetManager.load("textures/ui/buttons_hud_back_normal.png", Texture.class);
        globalAssetManager.load("textures/ui/leaderboard.png", Texture.class);

    }

    @Override
    public void create() {
        instance = this;

        try {
            NetworkClient.getInstance().connect("localhost", PvzServer.DEFAULT_PORT);
        } catch (IOException e) {
            // نمایش خطا به کاربر / fallback به حالت آفلاین
        }

        FileHandle file = Gdx.files.internal("pvz-assets/animations.json");
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        AnimationCatalog.setInstance(json.fromJson(AnimationCatalog.class, file));

        globalAssetManager.finishLoading();
        textureBank = new TextureBank("768", Gdx.files.internal("pvz-assets"));
        pamPlayer = new PamPlayer(textureBank, Gdx.files.internal("pvz-assets"));
        batch = new SpriteBatch();
        brightnessBatch = new SpriteBatch();
        Pixmap whitePixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        whitePixmap.setColor(1f, 1f, 1f, 1f);
        whitePixmap.fill();
        brightnessPixel = new Texture(whitePixmap);
        whitePixmap.dispose();
        applyWindowIcon();
        applyCustomCursor();

        java.util.HashMap<String, String> savedCreds = SaveManager.getInstance().load("session.json",
                java.util.HashMap.class);
        if (savedCreds != null) {
            String savedUsername = savedCreds.get("username");
            String savedPassword = savedCreds.get("password");
            if (savedUsername != null && savedPassword != null) {
                NetworkClient.getInstance().login(savedUsername, savedPassword, response -> {
                    if (response.success) {
                        User user = NetworkClient.getInstance().parsePayload(response, User.class);
                        AppContext.getInstance().setCurrentUser(user);
                        applyFullscreenSetting();
                        setScreen(new MainMenu(this));
                    } else {
                        SaveManager.getInstance().delete("session.json");
                        setScreen(new LoginMenu(this));
                    }
                });
                return;
            }
        }
        setScreen(new LoginMenu(this));
    }

    /** Toggles the desktop window between fullscreen and windowed mode. */
    public void applyFullscreen(boolean fullscreen) {
        try {
            if (fullscreen) {
                Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(displayMode);
            } else {
                Gdx.graphics.setWindowedMode(640, 480);
            }
        } catch (Exception e) {
            Gdx.app.error("PvZ2", "could not apply fullscreen setting", e);
        }
    }

    /**
     * Applies the fullscreen preference of the currently logged-in user at startup.
     */
    private void applyFullscreenSetting() {
        User user = AppContext.getInstance().getCurrentUser();
        if (user == null)
            return;
        applyFullscreen(user.getSetting().isFullscreen());
    }

    /**
     * Sets the window/taskbar icon from textures/ui/zombie_head.png.
     * That PNG is a green-screen sprite (no alpha), so the background green is
     * chroma-keyed
     * out at startup before handing the pixmap to the OS — otherwise the taskbar
     * shows a
     * solid green/white square instead of the cut-out head.
     */
    private void applyWindowIcon() {
        try {
            String iconPath = "textures/ui/zombie_head.png";
            if (!Gdx.files.internal(iconPath).exists())
                return;

            Pixmap source = new Pixmap(Gdx.files.internal(iconPath));
            Pixmap keyed = keyOutGreen(source);
            source.dispose();

            // Windows likes several sizes; downscale the keyed image into each.
            int[] sizes = { 128, 64, 32, 16 };
            Pixmap[] icons = new Pixmap[sizes.length];
            for (int i = 0; i < sizes.length; i++) {
                icons[i] = scaleTo(keyed, sizes[i], sizes[i]);
            }
            keyed.dispose();

            // The Graphics interface doesn't expose setIcon in this gdx version; the
            // desktop
            // backend (Lwjgl3Window.setIcon) does. Call it reflectively so core stays
            // backend-agnostic.
            Object graphics = Gdx.graphics;
            Object window = graphics.getClass().getMethod("getWindow").invoke(graphics);
            window.getClass().getMethod("setIcon", Pixmap[].class).invoke(window, (Object) icons);
            for (Pixmap icon : icons)
                icon.dispose();
        } catch (Exception e) {
            Gdx.app.error("PvZ2", "could not apply window icon", e);
        }
    }

    /**
     * Replaces the OS mouse cursor with textures/ui/cursor.png everywhere in-game.
     * The source is a large PNG (1112x607) with real alpha, so it's scaled down to
     * a
     * normal cursor size and handed to the OS.
     *
     * The hotspot is the single pixel the OS reports as the click point —
     * everything
     * else is just decoration. It must point at the VISUAL tip of the pointer, not
     * the
     * top-left of the sprite. These two constants describe that point as a fraction
     * (0.0 .. 1.0) of the original cursor.png: (0,0) = image top-left, (1,1) =
     * bottom-right.
     * Open cursor.png, eyeball where the tip is (a classic arrow tip near the
     * bottom-left
     * is roughly X=0.10, Y=0.90), and tune until clicks land exactly where the tip
     * points.
     */
    private static final float CURSOR_HOTSPOT_X = 0.10f;
    private static final float CURSOR_HOTSPOT_Y = 0.05f;
    private static final int CURSOR_MAX_SIZE = 90;

    private void applyCustomCursor() {
        try {
            String cursorPath = "textures/ui/cursor.png";
            if (!Gdx.files.internal(cursorPath).exists())
                return;

            Pixmap source = new Pixmap(Gdx.files.internal(cursorPath));
            float scale = Math.min(1f, CURSOR_MAX_SIZE / (float) Math.max(source.getWidth(), source.getHeight()));
            int w = Math.max(1, Math.round(source.getWidth() * scale));
            int h = Math.max(1, Math.round(source.getHeight() * scale));
            // Lwjgl3 rejects non-power-of-two cursor pixmaps, so the scaled sprite is
            // drawn at (0,0) onto a transparent power-of-two canvas of the same or larger
            // size.
            int canvasW = nextPowerOfTwo(w);
            int canvasH = nextPowerOfTwo(h);
            Pixmap cursor = new Pixmap(canvasW, canvasH, Pixmap.Format.RGBA8888);
            cursor.setBlending(Pixmap.Blending.None);
            for (int y = 0; y < h; y++) {
                int sy = Math.min(source.getHeight() - 1, (y * source.getHeight()) / h);
                for (int x = 0; x < w; x++) {
                    int sx = Math.min(source.getWidth() - 1, (x * source.getWidth()) / w);
                    cursor.drawPixel(x, y, source.getPixel(sx, sy));
                }
            }
            source.dispose();

            // Convert the source-relative hotspot into the scaled sprite's pixel space
            // (the sprite is drawn at (0,0) of the canvas, so canvas hotspot == sprite
            // hotspot).
            int hotspotX = Math.round(CURSOR_HOTSPOT_X * (w - 1) + 21);
            int hotspotY = Math.round(CURSOR_HOTSPOT_Y * (h - 1) + 8);

            Gdx.graphics.setCursor(Gdx.graphics.newCursor(cursor, hotspotX, hotspotY));
            cursor.dispose();
        } catch (Exception e) {
            Gdx.app.error("PvZ2", "could not apply custom cursor", e);
        }
    }

    private static int nextPowerOfTwo(int value) {
        int v = Math.max(1, value);
        while ((v & (v - 1)) != 0) {
            v++;
        }
        return v;
    }

    /** Removes the bright green background, turning matching pixels transparent. */
    private static Pixmap keyOutGreen(Pixmap src) {
        int w = src.getWidth(), h = src.getHeight();
        Pixmap out = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        out.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = src.getPixel(x, y);
                int r = (px >> 24) & 0xFF;
                int g = (px >> 16) & 0xFF;
                int b = (px >> 8) & 0xFF;
                // treat strongly-green pixels as background
                if (g > 80 && g > r * 1.25f && g > b * 1.25f) {
                    out.setColor(0f, 0f, 0f, 0f);
                } else {
                    out.setColor(r / 255f, g / 255f, b / 255f, 1f);
                }
                out.drawPixel(x, y);
            }
        }
        return out;
    }

    /** Nearest-neighbor scale (matches how the old window icons were sized). */
    private static Pixmap scaleTo(Pixmap src, int dstW, int dstH) {
        int sw = src.getWidth(), sh = src.getHeight();
        Pixmap out = new Pixmap(dstW, dstH, Pixmap.Format.RGBA8888);
        out.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < dstH; y++) {
            int sy = Math.min(sh - 1, (y * sh) / dstH);
            for (int x = 0; x < dstW; x++) {
                int sx = Math.min(sw - 1, (x * sw) / dstW);
                out.drawPixel(x, y, src.getPixel(sx, sy));
            }
        }
        return out;
    }

    @Override
    public void render() {
        super.render();
        textureBank.update();
        drawBrightnessOverlay();
    }

    /**
     * Applies the Settings screen's Brightness slider on top of whatever screen
     * just
     * rendered, so it's a real global effect instead of only visible on the
     * Settings
     * screen itself. Below 100% darkens the screen (black, increasing alpha); above
     * 100% gives a light wash (white, additive blend) since real pixels can't be
     * pushed
     * brighter than their own color without a proper lighting pass.
     */
    private void drawBrightnessOverlay() {
        User user = AppContext.getInstance().getCurrentUser();
        Setting setting = (user != null) ? user.getSetting() : null;
        float brightness = (setting != null) ? setting.getBrightness() : 1f;
        if (Math.abs(brightness - 1f) < 0.005f)
            return; // neutral, nothing to draw

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        brightnessProjection.setToOrtho2D(0, 0, width, height);
        brightnessBatch.setProjectionMatrix(brightnessProjection);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        brightnessBatch.begin();
        if (brightness < 1f) {
            brightnessBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            brightnessBatch.setColor(0f, 0f, 0f, 1f - brightness);
        } else {
            brightnessBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            brightnessBatch.setColor(1f, 1f, 1f, (brightness - 1f) * 0.5f);
        }
        brightnessBatch.draw(brightnessPixel, 0, 0, width, height);
        brightnessBatch.setColor(1f, 1f, 1f, 1f);
        brightnessBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (brightnessBatch != null)
            brightnessBatch.dispose();
        if (brightnessPixel != null)
            brightnessPixel.dispose();
        AudioManager.getInstance().disposeSounds();
    }

    public AssetManager getGlobalAssetManager() {
        return globalAssetManager;
    }
}
