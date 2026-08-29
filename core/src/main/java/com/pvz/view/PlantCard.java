package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;

import pvz.skin.PvzSkin;

/**
 * One collectible plant card: the packet art doubled as the button background, with the family
 * badge at the top-left, the plant level at the top-right, the sun cost and seed-packet counter
 * just above a full-width progress bar across the bottom edge. Locked cards are dimmed with a
 * gold lock badge in the top-right corner (matching IMAGE_UI_LOCK_SMALL_GOLD) but stay
 * clickable so the player can open the details and buy the plant there.
 */
public class PlantCard extends Button {

    public static final float WIDTH = 160f;
    public static final float HEIGHT = 105f;

    public final PlantData data;
    private final Image dimLayer;
    private final Image lockImage;
    private final ProgressBar xpBar;
    private final Label costLabel;
    private final Label levelLabel;
    private final Label packetsLabel;
    private final Table levelLayer;

    public PlantCard(PlantData data) {
        super(styleFor(data));
        this.data = data;
        setSize(WIDTH, HEIGHT);

        Skin skin = PvzSkin.get();
        Drawable fallback = skin.newDrawable("white_pixel", new Color(0.25f, 0.25f, 0.25f, 1f));

        Drawable packetDrawable = data.cardDrawable();
        if (packetDrawable == null) packetDrawable = PlantData.regionDrawableOr(data.cardImageId(), fallback);
        Image packet = new Image(packetDrawable);
        packet.setScaling(Scaling.fit);

        Image badge = new Image(PlantData.regionDrawableOr(data.familyImageId(), fallback));

        lockImage = new Image(PlantData.regionDrawableOr("IMAGE_UI_LOCK_SMALL_GOLD", fallback));
        lockImage.setScaling(Scaling.fit);

        dimLayer = new Image(skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.55f)));

        costLabel = new Label(String.valueOf(data.sunCost()), skin, "medium");
        costLabel.setColor(Color.WHITE);

        xpBar = new ProgressBar(0f, 1f, 0.01f, false, skin, "xp_green");
        xpBar.setValue(data.xpFraction());

        levelLabel = new Label("", skin, "medium");
        levelLabel.setColor(Color.WHITE);

        packetsLabel = new Label("", skin, "medium");
        packetsLabel.setColor(Color.WHITE);

        // ── center: packet art as a smaller icon, blue card background around it ─
        Table packetLayer = new Table();
        packetLayer.center();
        packetLayer.add(packet).size(96f, 96f);

        // ── top-left: family badge ──────────────────────────────────────────────
        Table badgeLayer = new Table();
        badgeLayer.top().left();
        badgeLayer.add(badge).size(36f, 36f).pad(3f);

        // ── top-right: gold lock on locked cards (matches IMAGE_UI_LOCK_SMALL_GOLD) ─
        Table lockLayer = new Table();
        lockLayer.top().right();
        lockLayer.add(lockImage).size(26f).pad(4f);

        // ── top-right: level pill ────────────────────────────────────────────────
        Table levelPill = new Table();
        levelPill.setBackground(skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.6f)));
        levelPill.add(levelLabel).pad(2f, 7f, 2f, 7f);
        levelLayer = new Table();
        levelLayer.top().right();
        levelLayer.add(levelPill).pad(4f);

        // ── bottom: info row over a full-width seed-packet progress bar ─────────
        Table infoRow = new Table();
        infoRow.add(costLabel).left();
        infoRow.add().expandX();
        infoRow.add(packetsLabel).right();

        Table bottomBar = new Table();
        bottomBar.bottom();
        bottomBar.add(infoRow).width(WIDTH - 16f).padBottom(3f);
        bottomBar.row();
        bottomBar.add(xpBar).width(WIDTH - 16f).height(7f);

        Stack stack = new Stack();
        stack.add(packetLayer);
        stack.add(dimLayer);
        stack.add(lockLayer);
        stack.add(badgeLayer);
        stack.add(levelLayer);
        stack.add(bottomBar);

        add(stack).size(WIDTH, HEIGHT);

        update();
    }

    private static ButtonStyle styleFor(PlantData data) {
        ButtonStyle style = new ButtonStyle();
        style.up = PlantData.regionDrawable(data.isBoosted() ? "IMAGE_UI_PACKETS_BOOST"
            : "IMAGE_UI_PACKETS_READY");
        style.over = PlantData.regionDrawable("IMAGE_UI_PACKETS_SELECT");
        style.checked = PlantData.regionDrawable("IMAGE_UI_PACKETS_SELECTED");
        return style;
    }

    /** Refreshes lock dimming, lock icon, sun cost, level and seed-packet progress from the save. */
    public void update() {
        boolean unlocked = data.isUnlocked();
        costLabel.setText(String.valueOf(data.sunCost()));
        dimLayer.setVisible(!unlocked);
        lockImage.setVisible(!unlocked);
        levelLayer.setVisible(unlocked); // avoid overlapping the top-right lock icon when locked
        xpBar.setValue(data.xpFraction());
        levelLabel.setText("LV " + data.getLevel());

        if (unlocked) {
            if (data.isMaxLevel()) {
                packetsLabel.setText("MAX");
            } else {
                packetsLabel.setText(data.seedPackets() + "/" + data.requiredSeedPackets());
            }
        } else {
            packetsLabel.setText("");
        }
    }
}
