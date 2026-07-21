package pvz.models.games.modes;

import pvz.models.games.GameContext;

/**
 * Interface representing a specific game mode (e.g., Standard, Vasebreaker, Beghouled).
 * Encapsulates only the lifecycle every mode shares. Mode-specific abilities
 * (placing cards, breaking vases, swapping gems, ...) are expressed as separate
 * {@code Capabilities} interfaces that a mode opts into individually, so this
 * interface never grows just because a new mini-game is added.
 */
public interface GameMode {
    /** Initialize mode-specific state (e.g., enable/disable mowers). */
    void initMode(GameContext context);

    /** Update mode-specific rules (Win/Loss conditions). */
    void updateMode(GameContext context);

    String renderMap(GameContext context);

    /** Whether this mode allows sky-dropped falling suns (e.g. Dark Ages disables them). */
    default boolean supportsFallingSuns() { return true; }

    String getCardsStatus(GameContext context);
}
