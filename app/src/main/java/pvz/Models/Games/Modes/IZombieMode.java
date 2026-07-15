package pvz.Models.Games.Modes;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.TestGameContext.GameContext;
import pvz.Models.Games.card.Card;

/**
 * I, Zombie mode implementation.
 */
public class IZombieMode implements GameMode {

    @Override
    public void initMode(GameContext context) {
        context.disableLawnMowers(); // Lawn mowers act as "Brains"
    }

    @Override
    public void updateMode(GameContext context) {
        // Win Condition: All brains (mowers) eaten
        boolean allBrainsEaten = true;
        for (int i = 0; i < context.getLanes(); i++) {
            if (!context.isLawnMowerUsed(i)) {
                allBrainsEaten = false;
                break;
            }
        }
        
        if (allBrainsEaten) {
            context.setGameOver(true);
            context.log("All brains eaten. I, Zombie wins!");
            return;
        }

        // Loss Condition: No active zombies, sun < cheapest zombie cost
        if (context.getZombies().isEmpty() && context.getSunAmount() < context.getCheapestZombieCost()) {
            context.setGameOver(true);
            context.log("No zombies left and not enough sun. I, Zombie loses!");
        }
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        // Only in the rightmost column
        return col == context.getColumns() - 1;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (isValidPlacement(context, col, lane, card)) {
            Zombie z = card.createZombie(col, lane);
            context.getZombies().add(z);
        }
    }
}
