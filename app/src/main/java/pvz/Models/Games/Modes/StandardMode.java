package pvz.Models.Games.Modes;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class StandardMode implements GameMode {
    private Wave currentWave;
    private List<Wave> waves;
    
    @Override
    public void initMode(GameContext context) {
        context.setupLawnMowers();
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
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        // Standard rule: cannot place on non-plantable tiles, etc.
        return context.getMap().getTile(col, lane).isPlantable(p);
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (isValidPlacement(context, col, lane, card)) {
            PlantCard plantCard = (PlantCard) card;
            Plant p = PlantFactory.create(plantCard.getPlant().getType(), col, lane , context);
            context.addPlant(p);
        }
    }
}
