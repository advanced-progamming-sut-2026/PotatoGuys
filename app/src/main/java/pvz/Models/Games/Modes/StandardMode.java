package pvz.Models.Games.Modes;

import java.util.List;

import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.card.Card;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class StandardMode implements GameMode {
    private Wave currentWave;
    private List<Wave> waves;
    

    public StandardMode(Level level){
        waves = level.getWaves();
        currentWave = waves.getFirst();
    }
    @Override
    public void initMode(GameContext context) {
        // context.setupLawnMowers();
    }

    @Override
    public void updateMode(GameContext context) {

        if (context.getZombies().isEmpty()) {
            if (waves.indexOf(currentWave) < waves.size() - 1) {
                currentWave = waves.get(waves.indexOf(currentWave) + 1);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
        }
    }
    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        throw new UnsupportedOperationException("Unimplemented method 'isValidPlacement'");
    }
    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        throw new UnsupportedOperationException("Unimplemented method 'handlePlacement'");
    }
}
