package com.pvz.view.game.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.pvz.models.AppContext;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.games.modes.variants.IZombieLocalMode;

import pvz.skin.PvzSkin;

/**
 * In-game HUD for the local two-player split I,Zombie mode.
 *
 * <p>
 * The plant side and the zombie side share one screen but keep separate sun
 * pools and never touch each other's cards:
 * <ul>
 * <li>Plant cards sit in the left tray (reused from {@link GameUiModal});
 * the plant's sun is shown in the top-left sun bank.</li>
 * <li>Zombie cards sit in a dedicated right-hand tray; the zombie's sun is
 * shown in a separate right-side sun bank.</li>
 * </ul>
 * The zombie player operates entirely with the keyboard, so the right tray
 * also exposes helpers to move selection up/down and to read the currently
 * selected zombie card (used by the controller).
 */
public class IZombieLocalUiModal extends GameUiModal {

    private final Table zombieCardsTable = new Table();
    private final Label zombieSunLabel;
    private final List<ZombieCard> zombieCardOrder = new ArrayList<>();
    private int zombieSelectedIndex = 0;

    public IZombieLocalUiModal(Runnable onPauseRequested) {
        super(onPauseRequested);

        // Zombie sun bank pinned top-right, just under the top bar.
        Stack zombieSunBank = new Stack();
        Image sunBg = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_background_3slice"));
        sunBg.setScaling(Scaling.stretch);
        Table sunContent = new Table();
        Image sunIcon = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_sun"));
        sunIcon.setScaling(Scaling.fit);
        zombieSunLabel = new Label("0", PvzSkin.get(), "big_outline");
        zombieSunLabel.setColor(Color.valueOf("7AFF7A"));
        zombieSunLabel.setFontScale(1.25f);
        zombieSunLabel.setAlignment(Align.center);
        sunContent.add(sunIcon).size(55f).padLeft(-9f);
        sunContent.add(zombieSunLabel).expandX().center().padRight(8f);
        zombieSunBank.add(sunBg);
        zombieSunBank.add(sunContent);

        // Right tray: zombie sun on top, then the vertical zombie card column.
        zombieCardsTable.top();
        zombieCardsTable.defaults().pad(1f).size(SLOT_WIDTH, SLOT_HEIGHT);

        Table rightPanel = new Table();
        rightPanel.top().right();
        rightPanel.add(zombieSunBank).width(150f).height(54f).padRight(20f).top().right().row();
        rightPanel.add(zombieCardsTable).padRight(15f).padTop(8f).top().right();

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().right();
        overlay.add(rightPanel).top().right().padTop(72f);
        addActor(overlay);

        // Pull the plant tray out of the base cell row (which starts it on top
        // of the sun-bank row, ~62px from the screen top) and pin it at exactly
        // 70px, so the left plant cards line up with the top of the zombie sun
        // bank on the right.
        cardsBarTable.remove();
        Table leftTrayOverlay = new Table();
        leftTrayOverlay.setFillParent(true);
        leftTrayOverlay.top().left();
        leftTrayOverlay.add(cardsBarTable).top().left().padLeft(15f).padTop(70f);
        addActor(leftTrayOverlay);
    }

    private void populateZombieTray() {
        zombieCardsTable.clearChildren();
        zombieSlotByCard.clear();
        zombieCooldownOverlayByCard.clear();
        zombieCardOrder.clear();

        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null)
            return;

        loadPlantData();

        for (Card card : context.getCards()) {
            if (card instanceof ZombieCard zc) {
                zombieCardOrder.add(zc);
                Table zombieSlot = buildZombieSlot(zc);
                // Split mode: zombies are keyboard-only. Never let the mouse
                // select or place a zombie, keeping the two sides independent.
                zombieSlot.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                zombieCardsTable.add(zombieSlot).row();
            }
        }
    }

    @Override
    public void initCards() {
        // Split layout: plant cards go straight into the left tray and zombie
        // cards straight into the right tray. We deliberately do NOT call the
        // base initCards(): it would drop the zombie slots into the left tray
        // first, and merely removing their actors afterwards leaves empty (but
        // still sized) cells that push the plant cards down the screen.
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null)
            return;

        cardsBarTable.clearChildren();
        slotByCard.clear();
        cooldownOverlayByCard.clear();
        selectedCard = null;
        selectedZombieCard = null;

        loadPlantData();

        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc) {
                cardsBarTable.add(buildSlot(pc, false)).row();
            }
        }
        populateZombieTray();
        updateCardStyles();
    }

    @Override
    public void updateHud() {
        super.updateHud();
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null)
            return;
        if (context.getMode() instanceof IZombieLocalMode izMode) {
            zombieSunLabel.setText(String.valueOf(izMode.getZombieSun()));
        } else {
            zombieSunLabel.setText("0");
        }
    }

    // ---- keyboard-facing helpers for the zombie player ----

    public int getZombieCardCount() {
        return zombieCardOrder.size();
    }

    public ZombieCard getZombieCardAt(int index) {
        if (index < 0 || index >= zombieCardOrder.size())
            return null;
        return zombieCardOrder.get(index);
    }

    /** Cycles the highlighter up/down (-1 or +1) across zombie cards. */
    public void moveZombieSelection(int delta) {
        if (zombieCardOrder.isEmpty())
            return;
        zombieSelectedIndex = (zombieSelectedIndex + delta + zombieCardOrder.size()) % zombieCardOrder.size();
        syncSelectedZombieCard();
    }

    /** Marks the current highlighter position as the armed zombie card. */
    public void armSelectedZombie() {
        setSelectedZombieCard(getZombieCardAt(zombieSelectedIndex));
    }

    private void syncSelectedZombieCard() {
        for (int i = 0; i < zombieCardOrder.size(); i++) {
            Table slot = zombieSlotByCard.get(zombieCardOrder.get(i));
            if (slot == null)
                continue;
            if (i == zombieSelectedIndex) {
                slot.setColor(1f, 0.75f, 0.2f, 1f);
            } else if (selectedZombieCard == zombieCardOrder.get(i)) {
                slot.setColor(0.8f, 0.5f, 1f, 1f);
            } else {
                slot.setColor(1f, 1f, 1f, 1f);
            }
        }
    }

    @Override
    protected void updateCardStyles() {
        super.updateCardStyles();
        syncSelectedZombieCard();
    }

    /** Clears the selected plant cards so the mouse player's tray is clean. */
    public void clearPlantSelection() {
        setSelectedCard(null);
    }

    public boolean isZombieArmed() {
        return selectedZombieCard != null;
    }
}
