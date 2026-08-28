package com.pvz.models.games.modes;

import com.pvz.models.games.GameContext;

/**
 * Interface representing a specific game mode (e.g., Standard, Vasebreaker,
 * Beghouled).
 * Encapsulates only the lifecycle every mode shares. Mode-specific abilities
 * (placing cards, breaking vases, swapping gems, ...) are expressed as separate
 * {@code Capabilities} interfaces that a mode opts into individually, so this
 * interface never grows just because a new mini-game is added.
 */
public interface GameMode {
    /** Initialize mode-specific state (e.g., enable/disable mowers). */
    void initMode(GameContext context);

    /**
     * Update mode-specific rules (Win/Loss conditions).
     * 
     * @param dt TODO
     */
    void updateMode(GameContext context, float dt);

    /**
     * Whether this mode allows sky-dropped falling suns (e.g. Dark Ages disables
     * them).
     */
    default boolean supportsFallingSuns() {
        return true;
    }

    /** Whether this mode shows a wave progress bar. */
    default boolean hasProgressBar() {
        return false;
    }

    /** Current wave index (0-based). Only valid when hasProgressBar() is true. */
    default int getCurrentWaveIndex() {
        return 0;
    }

    /** Total number of waves. Only valid when hasProgressBar() is true. */
    default int getTotalWaves() {
        return 0;
    }

    /** Total zombie count across all waves. Used for progress percentage. */
    default int getTotalZombieCount() {
        return 0;
    }
}
