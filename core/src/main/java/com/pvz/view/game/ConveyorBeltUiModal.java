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
 *   <li>The tray is anchored to the top-left of the screen, its first card
 *       pushed down by {@link #TRAY_TOP_OFFSET} so it never overlaps the sun
 *       bank and never runs off the top edge.</li>
 *   <li>Cards accumulate downward: every new card is appended at the bottom.</li>
 *   <li>A freshly delivered card flies in from the bottom-right of the screen
 *       and slides up into the bottom of the tray.</li>
 *   <li>When a card is used (planted) it is drained out of the tray and the
 *       remaining cards condense to fill the gap.</li>
 * </ul>
 */
public class ConveyorBeltUiModal extends GameUiModal {

    private static final float TRAY_PAD_LEFT = 18f;
    private static final float TRAY_PAD_TOP = 26f;
    private static final float SLIDE_TIME = 0.42f;
    private static final float DRAIN_TIME = 0.28f;

    /**
     * Vertical gap between the top of the screen and the top of the first card.
     * Mirrors the offset the base {@link GameUiModal} uses to tuck the left-hand
     * tray under the sun bank, so the belt clears the top HUD and stays on screen.
     */
    private static final float TRAY_TOP_OFFSET = 64f;

    /** Stage size of the 1280x720 gameplay viewport. */
    private static final float STAGE_HEIGHT = 720f;

    private final Group trayGroup;
    private final List<PlantCard> trayCards = new ArrayList<>();
    private final Set<PlantCard> draining = new HashSet<>();

    public ConveyorBeltUiModal(Runnable onPauseRequested) {
        super(onPauseRequested);

        trayGroup = new Group();
        trayGroup.setTouchable(Touchable.enabled);

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().left();
        overlay.add(trayGroup).top().left().padLeft(TRAY_PAD_LEFT).padTop(TRAY_PAD_TOP);
        addActor(overlay);
    }

    /**
     * Group-local Y for the card at {@code index} counting from the top of the
     * tray. The tray group is anchored to the top-left of the screen, so the
     * group's (0, 0) sits at the top edge; every card therefore lives at a
     * <em>negative</em> offset that grows downward as cards stack, keeping the
     * first card just under the sun bank and the whole belt fully on-screen.
     */
    private float slotYForIndex(int index) {
        return -(TRAY_TOP_OFFSET + (index + 1) * SLOT_HEIGHT);
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
        reflow(false, null);
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
        List<PlantCard> freshlyAdded = new ArrayList<>();
        for (PlantCard pc : desired) {
            if (!slotByCard.containsKey(pc) && !draining.contains(pc)) {
                addedAny = true;
                freshlyAdded.add(pc);
            }
        }

        if (addedAny) {
            addFreshCards(freshlyAdded);
        }

        if (addedAny || removedAny) {
            // Animate the surviving cards into place. Newly delivered cards already
            // carry their own bottom-right entrance animation, so they are excluded
            // here to avoid snapping them to the final spot prematurely.
            reflow(true, freshlyAdded);
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

    /**
     * Builds a slot for the first batch of cards (no entry animation). The first
     * card sits just under the sun bank ({@link #TRAY_TOP_OFFSET}) and later cards
     * stack downward.
     */
    private void addSlotInitial(PlantCard pc) {
        Table slot = buildSlot(pc, true);
        slot.setSize(SLOT_WIDTH, SLOT_HEIGHT);
        slot.setPosition(0f, slotYForIndex(trayCards.size()));
        trayGroup.addActor(slot);
        trayCards.add(pc);
    }

    /**
     * Appends every newly delivered card to the bottom of the tray (they are added
     * to {@link #trayCards} in the same order they appear in the hand, so they
     * stack at the bottom, never the top). Each one starts at the bottom-right of
     * the screen and glides up into its slot.
     */
    private void addFreshCards(List<PlantCard> fresh) {
        for (PlantCard pc : fresh) {
            Table slot = buildSlot(pc, true);
            slot.setSize(SLOT_WIDTH, SLOT_HEIGHT);
            int index = trayCards.size();
            trayGroup.addActor(slot);
            trayCards.add(pc);

            float targetX = 0f;
            float targetY = slotYForIndex(index);
            // The belt is vertical: the card keeps the tray's x (centre of the
            // stack) and only rises from the bottom of the screen up to its slot.
            slot.setPosition(0f, -STAGE_HEIGHT - SLOT_HEIGHT);
            slot.addAction(Actions.moveTo(targetX, targetY, SLIDE_TIME));
        }
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
     * Places every live belt card at its final position
     * {@code y = TRAY_TOP_OFFSET + index * SLOT_HEIGHT}. Existing cards keep their
     * spot; freshly delivered cards arrive already animated, so this only snaps
     * positions to ground truth.
     */
    private void reflow(boolean animate, List<PlantCard> skip) {
        for (int i = 0; i < trayCards.size(); i++) {
            PlantCard pc = trayCards.get(i);
            if (skip != null && skip.contains(pc)) {
                continue;
            }
            Table slot = slotByCard.get(pc);
            if (slot == null) {
                continue;
            }
            float targetY = slotYForIndex(i);
            if (animate) {
                slot.clearActions();
                slot.addAction(Actions.moveTo(0f, targetY, SLIDE_TIME));
            } else {
                slot.setPosition(0f, targetY);
            }
        }
    }
}
