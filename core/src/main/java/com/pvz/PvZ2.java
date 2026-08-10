package com.pvz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.pvz.controller.AudioManager;
import com.pvz.enums.AudioPaths;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.SaveManager;
import com.pvz.view.MainMenu;
import com.pvz.view.RegisterMenu;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.util.HashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PvZ2 extends Game {
    public static TextureBank textureBank;
    public static PamPlayer pamPlayer;
    public static SpriteBatch batch;
    AssetManager globalAssetManager;
    public PvZ2(){
        globalAssetManager=new AssetManager();
        globalAssetManager.load("textures/backgrounds/MainMenu.png", Texture.class);
        globalAssetManager.load("textures/pvz2_logo_horizontal.png", Texture.class);
        globalAssetManager.load("textures/ui/news_button.png", Texture.class);
        globalAssetManager.load("news_preview/news_selected2.png", Texture.class);
        globalAssetManager.load("textures/ui/buttons_hud_back_normal.png", Texture.class);
        globalAssetManager.load("textures/ui/leaderboard.png", Texture.class);
        String savedUsername = SaveManager.getInstance().load("session.json", String.class);
        if (savedUsername != null) {
            HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
            if (usernames != null) {
                String id = usernames.get(savedUsername);
                if (id != null) {
                    User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
                    if (user != null) {
                        user.refreshQuestLog();
                        AppContext.getInstance().setCurrentUser(user);
                        System.out.println("Welcome back, " + user.getNickName() + "!");
                        return;
                    }
                }
            }
            SaveManager.getInstance().delete("session.json");
        }
    }
    @Override
    public void create() {
        globalAssetManager.finishLoading();
        textureBank=new TextureBank("768",Gdx.files.internal("./assets/pvz-assets/"));
        pamPlayer=new PamPlayer(textureBank,Gdx.files.internal("./assets/pvz-assets/"));
        batch=new SpriteBatch();
        applyWindowIcon();
        applyCustomCursor();
        if (AppContext.getInstance().getCurrentUser()==null){
            setScreen(new RegisterMenu(this));
        } else {
            setScreen(new MainMenu(this));
        }
    }

    /**
     * Sets the window/taskbar icon from textures/ui/zombie_head.png.
     * That PNG is a green-screen sprite (no alpha), so the background green is chroma-keyed
     * out at startup before handing the pixmap to the OS — otherwise the taskbar shows a
     * solid green/white square instead of the cut-out head.
     */
    private void applyWindowIcon() {
        try {
            String iconPath = "textures/ui/zombie_head.png";
            if (!Gdx.files.internal(iconPath).exists()) return;

            Pixmap source = new Pixmap(Gdx.files.internal(iconPath));
            Pixmap keyed = keyOutGreen(source);
            source.dispose();

            // Windows likes several sizes; downscale the keyed image into each.
            int[] sizes = {128, 64, 32, 16};
            Pixmap[] icons = new Pixmap[sizes.length];
            for (int i = 0; i < sizes.length; i++) {
                icons[i] = scaleTo(keyed, sizes[i], sizes[i]);
            }
            keyed.dispose();

            // The Graphics interface doesn't expose setIcon in this gdx version; the desktop
            // backend (Lwjgl3Window.setIcon) does. Call it reflectively so core stays
            // backend-agnostic.
            Object graphics = Gdx.graphics;
            Object window = graphics.getClass().getMethod("getWindow").invoke(graphics);
            window.getClass().getMethod("setIcon", Pixmap[].class).invoke(window, (Object) icons);
            for (Pixmap icon : icons) icon.dispose();
        } catch (Exception e) {
            Gdx.app.error("PvZ2", "could not apply window icon", e);
        }
    }

    /**
     * Replaces the OS mouse cursor with textures/ui/cursor.png everywhere in-game.
     * The source is a large PNG (1112x607) with real alpha, so it's scaled down to a
     * normal cursor size and handed to the OS. The hotspot sits at the top-left (0,0),
     * matching a default arrow — tweak {@code HOTSPOT_X}/{@code HOTSPOT_Y} if the
     * pointer feels offset.
     */
    private void applyCustomCursor() {
        try {
            String cursorPath = "textures/ui/cursor.png";
            if (!Gdx.files.internal(cursorPath).exists()) return;

            final int HOTSPOT_X = 0;
            final int HOTSPOT_Y = 0;
            final int MAX_SIZE = 90;

            Pixmap source = new Pixmap(Gdx.files.internal(cursorPath));
            float scale = Math.min(1f, MAX_SIZE / (float) Math.max(source.getWidth(), source.getHeight()));
            int w = Math.max(1, Math.round(source.getWidth() * scale));
            int h = Math.max(1, Math.round(source.getHeight() * scale));
            // Lwjgl3 rejects non-power-of-two cursor pixmaps, so the scaled sprite is
            // drawn at (0,0) onto a transparent power-of-two canvas of the same or larger size.
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

            Gdx.graphics.setCursor(Gdx.graphics.newCursor(cursor, HOTSPOT_X, HOTSPOT_Y));
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
    }

    public AssetManager getGlobalAssetManager(){
        return globalAssetManager;
    }
}
