package com.pvz.view.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import com.pvz.models.AppContext;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;

/**
 * In-game HUD specialised for {@code ConveyorBeltMode}.
 *
 * <p>
 * Reuses the entire global HUD from {@link GameUiModal} (top bar, sun bank,
 * plant-food dots, wallet, wave meter, shovel, pause) but replaces the static
 * left-hand seed tray with a vertical "conveyor belt":
 * <ul>
 *   <li>The tray is anchored to the bottom of the screen instead of the top.</li>
 *   <li>Cards accumulate from the bottom upward, like a belt that fills up.</li>
 *   <li>A freshly delivered plant rises in from below the screen edge and slides
 *       into place while the existing cards nudge up to make room.</li>
 *   <li>When a card is used (planted) it is drained out of the tray and the
 *       remaining cards condense to fill the gap.</li>
 * </ul>
 */
public class ConveyorBeltUiModal extends GameUiModal {

    private static final float TRAY_PAD_LEFT = 18f;
    private static final float TRAY_PAD_BOTTOM = 26f;
    private static final float SLIDE_TIME = 0.38f;
    private static final float DRAIN_TIME = 0.28f;

    private final Group trayGroup;
    private final List<PlantCard> trayCards = new ArrayList<>();
    private final Set<PlantCard> draining = new HashSet<>();

    public ConveyorBeltUiModal(Runnable onPauseRequested) {
        super(onPauseRequested);

        trayGroup = new Group();
        trayGroup.setTouchable(Touchable.enabled);

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.bottom().left();
        overlay.add(trayGroup).bottom().left().padLeft(TRAY_PAD_LEFT).padBottom(TRAY_PAD_BOTTOM);
        addActor(overlay);
    }

    @Override
    public void initCards() {
        loadPlantData();
        trayGroup.clearChildren();
        trayCards.clear();
        draining.clear();
        slotByCard.clear();
        cooldownOverlayByCard.clear();
        selectedCard = null;
        setSelectedZombieCard(null);

        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) {
            return;
        }

        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc && !slotByCard.containsKey(pc)) {
                addSlotInitial(pc);
            }
        }
        reflow(false);
        updateCardStyles();
    }

    @Override
    public void syncCards() {
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) {
            return;
        }

        List<PlantCard> desired = new ArrayList<>();
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc && !draining.contains(pc)) {
                desired.add(pc);
            }
        }

        boolean removedAny = false;
        Iterator<PlantCard> it = trayCards.iterator();
        while (it.hasNext()) {
            PlantCard pc = it.next();
            if (!desired.contains(pc)) {
                it.remove();
                Table slot = slotByCard.remove(pc);
                cooldownOverlayByCard.remove(pc);
                draining.add(pc);
                drainSlot(slot, pc);
                if (selectedCard == pc) {
                    selectedCard = null;
                }
                removedAny = true;
            }
        }

        boolean addedAny = false;
        for (PlantCard pc : desired) {
            if (!slotByCard.containsKey(pc) && !draining.contains(pc)) {
                addSlotFresh(pc);
                addedAny = true;
            }
        }

        if (addedAny || removedAny) {
            reflow(true);
            updateCardStyles();
        }
    }

    /**
     * The base HUD already drives the sun / plant-food / wallet / wave / cooldown
     * updates and, via virtual dispatch of {@link #syncCards()}, keeps the belt
     * in sync. We only need to forward to it.
     */
    @Override
    public void updateHud() {
        super.updateHud();
    }

    /** Builds a slot for the first batch of cards (no entry animation). */
    private void addSlotInitial(PlantCard pc) {
        Table slot = buildSlot(pc, true);
        slot.setSize(SLOT_WIDTH, SLOT_HEIGHT);
        slot.setPosition(0f, trayCards.size() * SLOT_HEIGHT);
        trayGroup.addActor(slot);
        trayCards.add(pc);
    }

    /**
     * Adds a just-delivered card below the visible tray. The subsequent
     * {@link #reflow(boolean)} then slides the whole stack up, so the new card
     * "emerges" from the bottom of the screen while the older cards rise to make
     * room — a continuous filling belt.
     */
    private void addSlotFresh(PlantCard pc) {
        Table slot = buildSlot(pc, true);
        slot.setSize(SLOT_WIDTH, SLOT_HEIGHT);
        int index = trayCards.size();
        slot.setPosition(0f, index * SLOT_HEIGHT - SLOT_HEIGHT - 60f);
        trayGroup.addActor(slot);
        trayCards.add(pc);
    }

    /**
     * Animates a used card out of the tray (slides down + fades), then drops it
     * from the live {@code draining} bookkeeping.
     */
    private void drainSlot(Table slot, PlantCard pc) {
        if (slot == null) {
            draining.remove(pc);
            return;
        }
        slot.setTouchable(Touchable.disabled);
        slot.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.moveBy(0f, -SLOT_HEIGHT - 50f, DRAIN_TIME),
                        Actions.fadeOut(0.22f)),
                Actions.removeActor(),
                Actions.run(() -> draining.remove(pc))));
    }

    /**
     * Repositions every live belt slot to its target {@code y = index * SLOT_HEIGHT}.
     * When {@code animate} is true the slots glide up to their new spots, which
     * produces both the "belt rises as a card is delivered" and the "tray
     * condenses after a card is planted" motions.
     */
    private void reflow(boolean animate) {
        for (int i = 0; i < trayCards.size(); i++) {
            Table slot = slotByCard.get(trayCards.get(i));
            if (slot == null) {
                continue;
            }
            float targetY = i * SLOT_HEIGHT;
            if (animate) {
                slot.clearActions();
                slot.addAction(Actions.moveTo(0f, targetY, SLIDE_TIME));
            } else {
                slot.setPosition(0f, targetY);
            }
        }
    }
}
