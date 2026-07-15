package pvz.Models.Games.Modes;

import java.util.List;

import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.NormalLevel;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class NormalMode implements GameMode {
    private Wave currentWave;
    private List<Wave> waves;
    private List<PlantCard> plantCards;
    private Boolean[] lawnMower;
    
    public NormalMode(Level level){
        if(level instanceof NormalLevel normalLevel){
            waves = normalLevel.getWaves();
        }
        currentWave = waves.getFirst();
    }
    @Override
    public void initMode(GameContext context) {
        SetupLawnMowers();
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
            return;
        }

        currentWave.updateWave(context);
    }
    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        throw new UnsupportedOperationException("Unimplemented method 'isValidPlacement'");
    }
    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        throw new UnsupportedOperationException("Unimplemented method 'handlePlacement'");
    }

    private void SetupLawnMowers(){
        int lanes = 5; 
        lawnMower = new Boolean[lanes];
        for (int i = 0; i < lanes; i++) {
            lawnMower[i] = true; // All lawn mowers are initially available
        }
    }

    public void runLawnMowers(GameContext context , int lane){   
        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE);
            context.removeZombie(zombie);
            context.log("Lawn mower in lane " + lane + " ran over a zombie!");
        });
    }

    public List<Wave> getWaves() {
        return waves;
    }
    public void setWaves(List<Wave> waves) {
        this.waves = waves;
    }
    public List<PlantCard> getPlantCards() {
        return plantCards;
    }
    public void setPlantCards(List<PlantCard> plantCards) {
        this.plantCards = plantCards;
    }
    public void addPlantCard(PlantCard newCard){
        this.plantCards.add(newCard);
    }
}
