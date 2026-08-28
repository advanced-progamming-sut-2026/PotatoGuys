package com.pvz.models.games.modes.capabilities;

import com.pvz.models.games.GameContext;

/**
 * Capability trait for game modes that support smashing vases open
 * (Vasebreaker).
 */
public interface VaseBreaker {

    /**
     * Break the vase at the given tile, if any, revealing/releasing its contents.
     */
    void breakVase(GameContext context, int col, int lane);
}
