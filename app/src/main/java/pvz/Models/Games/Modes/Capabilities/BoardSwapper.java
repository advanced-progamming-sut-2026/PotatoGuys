package pvz.Models.Games.Modes.Capabilities;

import pvz.Models.Games.GameContext;

/**
 * Capability trait for game modes that support swapping two board cells
 * (Beghouled match-3).
 */
public interface BoardSwapper {

    /** Swap the contents of two (typically adjacent) board cells. */
    void swapPlants(GameContext context, int x1, int y1, int x2, int y2);
}
