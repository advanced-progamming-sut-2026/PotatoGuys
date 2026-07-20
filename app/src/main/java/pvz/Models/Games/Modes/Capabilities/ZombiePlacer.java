package pvz.Models.Games.Modes.Capabilities;

import pvz.Models.Games.GameContext;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.ZombieCard;

public interface ZombiePlacer {

    /** Check if a card can legally be placed at the given tile right now. */
    boolean isValidPlacement(GameContext context, int col, int lane, Card card);

    /** Perform placement of the card (subtract sun, spawn the plant, reset cooldown). */
    void handlePlacement(GameContext context, int col, int lane, Card card);

    /** Look up an available seed-packet card by plant type name, or {@code null} if none matches. */
    ZombieCard findCard(GameContext context ,String plantType);

}