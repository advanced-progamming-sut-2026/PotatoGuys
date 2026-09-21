package com.pvz.view.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.pvz.models.AppContext;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.card.ZombieCard;

/**
 * In-game HUD for the standard seed-packet tray, used by {@code NormalMode}
 * (and the other modes that hand the player a fixed deck: TimedWar, VaseBreaker,
 * DeadLine, SaveOurSeeds, Beghouled, WallnutBowling).
 *
 * <p>
 * Everything the base {@link GameUiModal} shares with every mode stays there
 * (top bar, sun bank, wave meter, wallet, pause, plant food, shovel). This
 * class owns only the left-hand static seed tray: the vertical column of full
 * plant/zombie cards hugging the left edge under the sun bank, plus its card
 * insertion/removal bookkeeping.
 *
 * <p>
 * It is also the natural base for the I,Zombie HUDs, which reuse the same
 * static tray (the plant side in local split mode, and whichever side the
 * online match assigns to the player).
 */
public class NormalUiModal extends GameUiModal {

    protected final Table cardsBarTable;

    public NormalUiModal(Runnable onPauseRequested) {
        super(onPauseRequested);

        // Seed-packet tray: vertical column pinned to the left edge, below the top bar.
        cardsBarTable = new Table();
        cardsBarTable.top();
        cardsBarTable.defaults().pad(-2f).size(SLOT_WIDTH, SLOT_HEIGHT);

        Table leftColumnWrapper = new Table();
        leftColumnWrapper.top().left();
        leftColumnWrapper.add(cardsBarTable);

        // Seed-packet tray tucked right under the sun bank: the top bar only keeps
        // the sun row, and padTop(10) starts the first card just under the sun
        // amount display (the food row moved to the bottom overlay).
        add(leftColumnWrapper).left().top().colspan(2).padLeft(15).padTop(-17);
    }

    @Override
    public void initCards() {
        cardsBarTable.clearChildren();
        slotByCard.clear();
        cooldownOverlayByCard.clear();
        zombieSlotByCard.clear();
        zombieCooldownOverlayByCard.clear();
        selectedCard = null;
        selectedZombieCard = null;

        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null)
            return;

        com.pvz.models.MatchSession __ms = com.pvz.models.AppContext.getInstance().getMatchSession();
        com.badlogic.gdx.Gdx.app.log("CardsDebug", "cards=" + context.getCards().size()
                + " matchSession=" + (__ms == null ? "null" : __ms.getMyRole())
                + " isNetworked=" + (__ms != null));

        loadPlantData();

        for (Card card : context.getCards()) {
            com.pvz.models.MatchSession matchSession = com.pvz.models.AppContext.getInstance().getMatchSession();
            if (matchSession != null) {
                boolean isPlantCard = card instanceof com.pvz.models.games.card.PlantCard;
                boolean isZombieCard = card instanceof com.pvz.models.games.card.ZombieCard;
                com.pvz.network.PlayerRole myRole = matchSession.getMyRole();
                if (isPlantCard && myRole != com.pvz.network.PlayerRole.PLANT)
                    continue;
                if (isZombieCard && myRole != com.pvz.network.PlayerRole.ZOMBIE)
                    continue;
            }
            if (card instanceof PlantCard pc) {
                cardsBarTable.add(buildSlot(pc, false)).row();
            } else if (card instanceof ZombieCard zc) {
                cardsBarTable.add(buildZombieSlot(zc)).pad(1f).row();
            }
        }
        updateCardStyles();
    }

    @Override
    public void syncCards() {
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null)
            return;

        com.pvz.models.MatchSession matchSession = com.pvz.models.AppContext.getInstance().getMatchSession();
        com.pvz.network.PlayerRole myRole = matchSession != null ? matchSession.getMyRole() : null;

        // ── Detect cards that disappeared from the context and rebuild the
        // bar so their slots (and any leftover empty cells) are removed.
        // e.g. VaseBreaker / ConveyorBelt used a card and it was removed
        // via ctx.removeCard.
        boolean removedAny = false;
        for (PlantCard pc : new java.util.ArrayList<>(slotByCard.keySet())) {
            if (!context.getCards().contains(pc)) {
                removedAny = true;
                break;
            }
        }
        if (removedAny) {
            rebuildCardBar(context, matchSession, myRole);
            return;
        }

        // ── Add slots for cards that are in the context but not yet in the UI ──
        boolean changed = false;
        for (Card card : context.getCards()) {
            if (matchSession != null) {
                if (card instanceof PlantCard && myRole != com.pvz.network.PlayerRole.PLANT)
                    continue;
                if (card instanceof ZombieCard && myRole != com.pvz.network.PlayerRole.ZOMBIE)
                    continue;
            }
            if (card instanceof PlantCard pc && !slotByCard.containsKey(pc)) {
                cardsBarTable.add(buildSlot(pc, false)).row();
                changed = true;
            }
        }
        if (changed) {
            updateCardStyles();
        }
    }

    /** Clears the card bar and re-adds every current card in context order. */
    private void rebuildCardBar(GameContext context,
            com.pvz.models.MatchSession matchSession,
            com.pvz.network.PlayerRole myRole) {
        cardsBarTable.clearChildren();
        slotByCard.clear();
        cooldownOverlayByCard.clear();
        selectedCard = null;
        for (Card card : context.getCards()) {
            if (matchSession != null) {
                if (card instanceof PlantCard && myRole != com.pvz.network.PlayerRole.PLANT)
                    continue;
                if (card instanceof ZombieCard && myRole != com.pvz.network.PlayerRole.ZOMBIE)
                    continue;
            }
            if (card instanceof PlantCard pc) {
                cardsBarTable.add(buildSlot(pc, false)).row();
            }
        }
        updateCardStyles();
    }
}
