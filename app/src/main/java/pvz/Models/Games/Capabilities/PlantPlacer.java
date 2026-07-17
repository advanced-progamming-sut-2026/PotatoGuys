package pvz.Models.Games.Capabilities;

import pvz.Models.Games.GameContext;
import pvz.Models.Games.card.Card;

/**
 * Capability trait for game modes that let the player place seed-packet
 * cards on the board (standard lawn defense, Zombotany, ...).
 */
public interface PlantPlacer {

    /** Check if a card can legally be placed at the given tile right now. */
    boolean isValidPlacement(GameContext context, int col, int lane, Card card);

    /** Perform placement of the card (subtract sun, spawn the plant, reset cooldown). */
    void handlePlacement(GameContext context, int col, int lane, Card card);

    /** Look up an available seed-packet card by plant type name, or {@code null} if none matches. */
    Card findCard(GameContext context , String plantType);

}
