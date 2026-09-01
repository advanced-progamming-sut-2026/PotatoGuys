package com.pvz.view.game.ui;

import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.pvz.network.game.StickerMessage;
import com.pvz.view.MenuUiKit;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * PvP sticker picker for online I,Zombie matches: a small animated-tv-button
 * pinned above the quick-chat toggle opens a box with three looping zombie
 * stickers. Picking one sends a {@link StickerMessage} to the opponent, who
 * plays the chosen clip for {@code StickerMessage.seconds} seconds on its own
 * screen; the sender's screen shows nothing (mirroring quick-chat).
 *
 * <p>
 * The box shares the Travel-Log quest-card look: a {@link BorderedTable} frame
 * (rounded brown border) with a solid brown rounded interior.
 */
public class StickerUi extends Table {

    private static final float STICKER_SIZE = 50f;
    private static final float STICKER_PAD_RIGHT = 20f;
    /** Pinned above the quick-chat toggle (82 + 50) with an 8px gap. */
    private static final float STICKER_PAD_BOTTOM = 140f;

    private static final float BOX_MARGIN = 20f;
    private static final float BOX_CLEARANCE = 6f;
    private static final float CELL_SIZE = 150f;
    /** How long a picked sticker stays on the opponent's screen. */
    private static final float STICKER_SECONDS = 3f;

    /** Animated-tv icon shown on the toggle button. */
    private static final String TOGGLE_TEX = "textures/ui/event_icon_valenbrainz_up.png";

    private static final String[] PAM_PATHS = {
            "768/FULL/ZOMBIE/ZOMBIE_MODERN_SUPERFAN/ZOMBIE_MODERN_SUPERFAN.PAM",
            "768/FULL/ZOMBIE/ZOMBIE_CARNIE_MONKEY/ZOMBIE_CARNIE_MONKEY.PAM",
            "768/FULL/ZOMBIE/ZOMBIE_80S_BREAKDANCER/ZOMBIE_80S_BREAKDANCER.PAM"
    };
    private static final String[] PAM_CLIPS = { "eat", "walk", "jam_idle" };
    private static final float[] PAM_SCALES = { 1f, 1f, 1f };
    private final Consumer<StickerMessage> onStickerSend;
    private final Runnable onRaise;
    private final List<Texture> ownedTextures = new java.util.ArrayList<>();
    private final Texture toggleTexture;
    private final Table box;

    public StickerUi(Consumer<StickerMessage> onStickerSend, Runnable onRaise) {
        this.onStickerSend = onStickerSend;
        this.onRaise = onRaise;

        toggleTexture = loadToggleTexture();

        setFillParent(true);
        bottom().right();

        // Box added before the toggle cell so the box stays front-most in this
        // panel, above the received stickers beneath it.
        box = buildBox();
        box.setVisible(false);
        addActor(box);

        ImageButton toggle = new ImageButton(new TextureRegionDrawable(toggleTexture),
                new TextureRegionDrawable(toggleTexture), new TextureRegionDrawable(toggleTexture));
        toggle.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean handled = super.touchDown(event, x, y, pointer, button);
                event.stop();
                return handled;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleBox();
            }
        });

        // Toggle button placed directly via a cell (right/bottom), so no
        // fullscreen overlay swallows clicks meant for the box.
        add(toggle).size(STICKER_SIZE).padRight(STICKER_PAD_RIGHT).padBottom(STICKER_PAD_BOTTOM);
    }

    /** True while the sticker box is open. */
    public boolean isBoxVisible() {
        return box.isVisible();
    }

    /** True when the open sticker box covers the stage point (sx, sy). */
    public boolean boxContains(float sx, float sy) {
        if (!box.isVisible())
            return false;
        return sx >= box.getX() && sx <= box.getX() + box.getWidth()
                && sy >= box.getY() && sy <= box.getY() + box.getHeight();
    }

    public void dispose() {
        for (Texture t : ownedTextures) {
            t.dispose();
        }
        ownedTextures.clear();
    }

    /**
     * The box: one row of three looping sticker previews, inside the quest-card
     * style (brown rounded frame + solid brown interior).
     */
    private Table buildBox() {
        Table content = new Table();
        for (int i = 0; i < PAM_PATHS.length; i++) {
            content.add(buildCell(i)).size(CELL_SIZE).pad(4f);
        }

        int innerW = Math.round(content.getPrefWidth()) + 20;
        int innerH = Math.round(content.getPrefHeight()) + 20;

        Image bg = new Image(roundedRectTexture(innerW, innerH, 20, new Color(0.36f, 0.24f, 0.12f, 1f)));
        bg.setBounds(17f, 16f, innerW, innerH);

        BorderedTable box = new BorderedTable();
        box.pad(10f, 10f, 10f, 10f);
        box.setTouchable(Touchable.enabled);
        box.addActor(bg);
        box.add(content);
        box.setSize(innerW + 34f, innerH + 32f);
        return box;
    }

    /** One looping preview; clicking it sends the sticker and closes the box. */
    private Table buildCell(int index) {
        PamClipActor preview = new PamClipActor(PAM_PATHS[index], PAM_CLIPS[index], -1f, -1f);

        Table cell = new Table();
        cell.add(preview).size(CELL_SIZE - 8f);
        cell.setTouchable(Touchable.enabled);
        cell.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean handled = super.touchDown(event, x, y, pointer, button);
                event.stop();
                return handled;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                hideBox();
                onStickerSend.accept(new StickerMessage(PAM_PATHS[index], PAM_CLIPS[index],
                    PAM_SCALES[index], STICKER_SECONDS));
            }
        });
        return cell;
    }

    private void toggleBox() {
        if (box.isVisible()) {
            hideBox();
        } else {
            float buttonTop = STICKER_PAD_BOTTOM + STICKER_SIZE;
            float x = 1280f - BOX_MARGIN - box.getWidth();
            box.setPosition(Math.max(0f, x), buttonTop + BOX_CLEARANCE);
            box.setColor(1f, 1f, 1f, 0f);
            box.setVisible(true);
            box.addAction(Actions.fadeIn(0.12f));
            box.toFront();
            if (onRaise != null) {
                onRaise.run();
            }
        }
    }

    private void hideBox() {
        box.setVisible(false);
        box.clearActions();
    }

    private Texture loadToggleTexture() {
        if (Gdx.files.internal(TOGGLE_TEX).exists()) {
            Texture t = new Texture(Gdx.files.internal(TOGGLE_TEX));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            ownedTextures.add(t);
            return t;
        }
        Texture fallback = MenuUiKit.solidTexture(new Color(0.75f, 0.2f, 0.2f, 1f));
        ownedTextures.add(fallback);
        return fallback;
    }

    /** Solid rounded-rect PNG helper, matching the Travel Log quest palette. */
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
}