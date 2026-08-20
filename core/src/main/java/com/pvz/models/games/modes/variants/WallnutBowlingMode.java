package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.WallnutBowlingLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.user.MyPlant;

/**
 * Wallnut Bowling mini-game mode.
 * Plants (Wallnut, Explodeonut) are delivered via conveyor belt and can only be
 * placed up to a red deadline line. No sun falls from the sky.
 */
public class WallnutBowlingMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private final int deadlineColumn;

    private int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 10;
    private static final int SPAWN_INTERVAL_TICKS = 6 * TICKS_PER_SECOND;

    private static final List<PlantType> BOWLING_PLANTS = List.of(
            PlantType.Wallnut,
            PlantType.Explodeonut);

    public WallnutBowlingMode(Level level) {
        if (level instanceof WallnutBowlingLevel wbLevel) {
            this.waves = wbLevel.getWaves();
            this.deadlineColumn = wbLevel.getDeadlineColumn();
        } else {
            this.deadlineColumn = 5;
            this.waves = new ArrayList<>();
        }
        if (waves != null && !waves.isEmpty()) {
            currentWave = waves.get(0);
        }
    }

    @Override
    public void initMode(GameContext context) {
        addRandomBowlingCard(context);
        tickCounter = 0;
        context.log("=== WALLNUT BOWLING MINI-GAME ===");
        context.log("Place bowling nuts up to the RED LINE (Column " + deadlineColumn + ").");
        context.log("No sun will fall from the sky. Conveyor belt delivers bowling plants!");
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        tickCounter++;
        if (tickCounter >= SPAWN_INTERVAL_TICKS) {
            addRandomBowlingCard(context);
            tickCounter = 0;
        }

        if (currentWave != null && currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Congratulations! You survived the Wallnut Bowling!");
            }
            return;
        }

        if (currentWave != null && !currentWave.isDone()) {
            currentWave.updateWave(context, 0);
        }

        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= 0f) {
                context.setGameOver(true);
                context.log("Brain has eaten!");
                context.removeZombie(z);
            }
        }

        for (Sun sun : new ArrayList<>(context.getSuns())) {
            if (sun.isDone()) {
                context.removeSun(sun);
            }
        }
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        if (card == null) {
            context.log("[Placement Failed] Selected card is null.");
            return false;
        }

        if (col < 0 || col >= context.getMap().getColumns() || lane < 0 || lane >= context.getMap().getLanes()) {
            context.log("[Placement Failed] Out of bounds: (" + col + ", " + lane + ")");
            return false;
        }

        if (col >= deadlineColumn) {
            context.log("[Placement Failed] Cannot plant beyond the RED LINE (Column " + deadlineColumn + ").");
            return false;
        }

        if (!context.getPlantsAt(col, lane).isEmpty()) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") is already occupied.");
            return false;
        }

        if (!context.getTileAt(col, lane).isPlantable(card)) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") does not support planting "
                    + card.getPlant().getType());
            return false;
        }

        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        if (!(card instanceof PlantCard plantCard)) {
            context.log("Error: card is not a plant card.");
            return;
        }

        Plant plant = new PlantFactory().create(
                plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.removeCard(card);
        context.log(plantCard.getPlant().getType() + " bowled at (" + col + ", " + lane + ").");
    }

    @Override
    public PlantCard findCard(GameContext context, String plantType) {
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard plantCard) {
                if (plantCard.getPlant().getType().toString().equalsIgnoreCase(plantType)) {
                    return plantCard;
                }
            }
        }
        return null;
    }

    @Override
    public String getCardsStatus(GameContext context) {
        List<Card> currentCards = context.getCards();
        if (currentCards.isEmpty()) {
            return "Conveyor belt is empty. Waiting for the next bowling nut...";
        }

        StringBuilder sb = new StringBuilder("=== Bowling Conveyor Belt ===\n");
        for (int i = 0; i < currentCards.size(); i++) {
            if (currentCards.get(i) instanceof PlantCard pc) {
                String boostLabel = pc.getPlant().isBoosted() ? " [BOOSTED]" : "";
                sb.append(String.format("[%d] %s (Lv: %d) | Cost: Free%s\n",
                        i, pc.getPlant().getType().toString(), pc.getPlant().getLevel(), boostLabel));
            }
        }
        return sb.toString().trim();
    }

    private void addRandomBowlingCard(GameContext context) {
        Random random = new Random();
        PlantType randomType = BOWLING_PLANTS.get(random.nextInt(BOWLING_PLANTS.size()));

        MyPlant myPlant = new MyPlant();
        myPlant.setType(randomType);
        myPlant.setLevel(1);
        PlantCard conveyorCard = new PlantCard(myPlant, 0, 0);
        context.addCard(conveyorCard);
        context.log("Conveyor delivered: " + randomType);
    }
}
