package pvz.Models.Games.Modes;

import java.util.List;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieFactory;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.ZombieCard;


public class IZombieMode implements GameMode {
    private List<ZombieCard> zombieCards;

    public IZombieMode(Level level) {
        
    }

    @Override
    public void initMode(GameContext context) {
        // context.disableLawnMowers(); // Lawn mowers act as "Brains"
    }

    @Override
    public void updateMode(GameContext context) {
        // Win Condition: All brains (mowers) eaten
        boolean allBrainsEaten = true;
        for (int i = 0; i < context.getLanes(); i++) {
            // if (!context.isLawnMowerUsed(i)) {
            //     allBrainsEaten = false;
            //     break;
            // }
        }
        
        if (allBrainsEaten) {
            context.setGameOver(true);
            context.log("All brains eaten. I, Zombie wins!");
            return;
        }

        // Loss Condition: No active zombies, sun < cheapest zombie cost
        if (context.getZombies().isEmpty() && context.getCurrentSun() < getCheapestZombieCost()) {
            context.setGameOver(true);
            context.log("No zombies left and not enough sun. I, Zombie loses!");
        }
    }

    private int getCheapestZombieCost(){
        int cheapestCost = Integer.MAX_VALUE;
        for(ZombieCard card : zombieCards){
            if(cheapestCost > card.getCost()){
                cheapestCost = card.getCost();
            }
        }
        return cheapestCost;
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        // Only in the rightmost column
        return col == context.getColumns() - 1;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (isValidPlacement(context, col, lane, card)) {
            ZombieCard zCard = (ZombieCard) card;
            // Zombie z = new ZombieFactory().create(zCard.getZombieType().getAlias() , (float)col , lane , context , 1 , 1);
            // context.spawnZombie(z);
        }
    }

    @Override
    public String renderMap(GameContext context){
        return null;
    }
}
