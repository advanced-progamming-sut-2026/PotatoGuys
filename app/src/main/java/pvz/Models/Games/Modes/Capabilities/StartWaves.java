package pvz.Models.Games.Modes.Capabilities;

import pvz.Models.Games.GameContext;

/**
 * Capability trait for game modes that let the player place seed-packet
 * cards on the board (standard lawn defense, Zombotany, ...).
 */
public interface StartWaves {
    
    public void startZombieWaves(GameContext context);
    public boolean isPreparationPhase();
}
