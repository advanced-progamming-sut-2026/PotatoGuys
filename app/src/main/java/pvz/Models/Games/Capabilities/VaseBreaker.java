package pvz.Models.Games.Capabilities;

import pvz.Models.Games.GameContext;

/**
 * Capability trait for game modes that support smashing vases open
 * (Vasebreaker).
 */
public interface VaseBreaker {

    /** Break the vase at the given tile, if any, revealing/releasing its contents. */
    void breakVase(GameContext context, int col, int lane);

    void showVases(GameContext context);
}
