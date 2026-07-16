package pvz.Models.Games.Modes;

import pvz.Models.Games.GameContext;
import pvz.Models.Games.card.Card;

/**
 * Interface representing a specific game mode (e.g., Standard, IZombie).
 * Encapsulates game rules and win/loss conditions.
 */
public interface GameMode {
    /** Initialize mode-specific state (e.g., enable/disable mowers). */
    void initMode(GameContext context);
    
    /** Update mode-specific rules (Win/Loss conditions). */
    void updateMode(GameContext context);
    
    /** Check if card can be placed at given location based on mode rules. */
    boolean isValidPlacement(GameContext context, int col, int lane, Card card);
    
    /** Perform placement of card (subtract sun, add entity). */
    void handlePlacement(GameContext context, int col, int lane, Card card);

    String renderMap(GameContext context);

    /** Whether this mode allows sky-dropped falling suns (e.g. Dark Ages disables them). */
    default boolean supportsFallingSuns() { return true; }
}
