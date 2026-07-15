package pvz.Models.Games.Modes;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.map.Wave;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class StandardMode implements GameMode {
    private int currentWave = 1;
    private final int totalWaves = 5; // Example
    private final List<Wave> waves;
    
    @Override
    public void initMode(GameContext context) {
        context.setupLawnMowers();
    }

    @Override
    public void updateMode(GameContext context) {
        // Manage Waves (simplified logic)
        if (context.getZombies().isEmpty() && !context.isSpawningWaves()) {
            if (currentWave < totalWaves) {
                currentWave++;
                context.log("Wave " + currentWave + " started.");
            } else {
                // Win Condition
                context.setGameOver(true);
                context.log("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
        }
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        // Standard rule: cannot place on non-plantable tiles, etc.
        return context.getMap().getTile(col, lane).isPlantable();
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (isValidPlacement(context, col, lane, card)) {
            Plant p = PlantFactory.createPlant(card.getPlantType(), col, lane);
            context.addPlant(p);
        }
    }
}
