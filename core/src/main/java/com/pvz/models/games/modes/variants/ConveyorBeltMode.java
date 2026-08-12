package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pvz.controller.game.GameController;
import com.pvz.enums.AnsiColors;
import com.pvz.models.AppContext;
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
import com.pvz.models.games.levels.variants.ConveyorBeltLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.user.Collection;
import com.pvz.models.user.MyPlant;
import com.pvz.models.user.User;

/**
 * Standard game mode implementation for Conveyor Belt level.
 * Automatically spawns random unlocked plants every 12 seconds.
 */
public class ConveyorBeltMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;

    private int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 10;
    private static final int SPAWN_INTERVAL_TICKS = 12 * TICKS_PER_SECOND;

    public ConveyorBeltMode(Level level) {
        if (level instanceof ConveyorBeltLevel normalLevel) {
            waves = normalLevel.getWaves();
        } else {
            return;
        }
        currentWave = waves.get(0);
    }

    @Override
    public void initMode(GameContext context) {
        addRandomCard(context);
        tickCounter = 0;
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        tickCounter++;
        if (tickCounter >= SPAWN_INTERVAL_TICKS) {
            addRandomCard(context);
            tickCounter = 0;
        }

        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context, 0);
        }

        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);

            if (z.getX() <= 0f) {
                context.setGameOver(true);
                context.log("Brain has eaten");
                context.removeZombie(z);
            }

        }

        for (int i = 0; i < context.getSuns().size(); i++) {
            Sun sun = context.getSuns().get(i);
            if (sun.isDone()) {
                context.removeSun(sun);
                i--;
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

        if (!context.getPlantsAt(col, lane).isEmpty()) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") is already occupied by another plant.");
            return false;
        }

        if (!context.getTileAt(col, lane).isPlantable(card)) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") does not support planting "
                        + card.getPlant().getType());
            return false;
        }

        // ConveyorBeltMode: Plants are free (delivered by conveyor belt), no sun cost check needed.
        // Cards are also not subject to cooldown; they arrive on a timer.
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
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
    }

    @Override
    public String getCardsStatus(GameContext context) {
        List<Card> currentCards = context.getCards();
        if (currentCards.isEmpty()) {
            return "Conveyor belt is empty. Waiting for the next plant...";
        }

        StringBuilder sb = new StringBuilder("=== Conveyor Belt ===\n");
        for (int i = 0; i < currentCards.size(); i++) {
            if (currentCards.get(i) instanceof PlantCard pc) {
                String boostLabel = pc.getPlant().isBoosted() ? " [BOOSTED]" : "";
                sb.append(String.format("[%d] %s (Lv: %d) | Cost: Free%s\n",
                        i, pc.getPlant().getType().toString(), pc.getPlant().getLevel(), boostLabel));
            }
        }
        return sb.toString().trim();
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

    private void addRandomCard(GameContext context) {
        var currentUser = AppContext.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getProfile() == null
                || currentUser.getProfile().getCollection() == null) {
            context.log("Error: User profiles or collection is not loaded.");
            return;
        }

        Collection collection=currentUser.getProfile().getCollection();
        List<MyPlant> unlockedPlants = new ArrayList<>(collection.getUnlockedPlants());
        unlockedPlants.remove(collection.getPlant(PlantType.Sunflower));
        unlockedPlants.remove(collection.getPlant(PlantType.TwinSunflower));

        if (unlockedPlants == null || unlockedPlants.isEmpty()) {
            context.log("Conveyor Belt warning: Player has no unlocked plants!");
            return;
        }

        Random random = new Random();
        MyPlant randomPlant = unlockedPlants.get(random.nextInt(unlockedPlants.size()));

        PlantCard conveyorCard = new PlantCard(randomPlant, 0, 0);

        context.addCard(conveyorCard);
        context.log("Conveyor delivered a new card: " + randomPlant.getType().toString());
    }
}
