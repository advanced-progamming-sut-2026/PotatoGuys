package com.pvz.models.games.modes.variants;

import java.util.List;
import java.util.Random;

import com.pvz.controller.game.GameController;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.ConveyorBeltLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.user.MyPlant;

public class ConveyorBeltMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private List<MyPlant> availablePlants;

    private float stateTime = 0;
    private static final float SPAWN_INTERVAL = 5f;
    private static final int MAX_HAND_SIZE = 7;

    public ConveyorBeltMode(Level level) {
        if (level instanceof ConveyorBeltLevel beltLevel) {
            waves = beltLevel.getWaves();
            availablePlants = beltLevel.getAllowedPlantTypes();
        } else {
            throw new IllegalArgumentException("ConveyorBeltMode requires a ConveyorBeltLevel");
        }
        currentWave = waves.get(0);
    }

    @Override
    public boolean hasProgressBar() {
        return true;
    }

    @Override
    public int getCurrentWaveIndex() {
        return waves.indexOf(currentWave);
    }

    @Override
    public int getTotalWaves() {
        return waves.size();
    }

    @Override
    public int getTotalZombieCount() {
        return waves.stream().mapToInt(Wave::getTotalZombieCount).sum();
    }

    @Override
    public void initMode(GameContext context) {
        stateTime = 0;
        addRandomCard(context);
        if (currentWave != null) {
            currentWave.startWave(context);
            for (com.pvz.models.games.effects.ChapterEffect effect : context.getActiveEffects()) {
                effect.onWaveStart(currentWave, context);
            }
        }
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        stateTime += dt;
        if (stateTime >= SPAWN_INTERVAL) {
            if (context.getCards().size() < MAX_HAND_SIZE) {
                addRandomCard(context);
            }
            stateTime = 0;
        }

        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                for (com.pvz.models.games.effects.ChapterEffect effect : context.getActiveEffects()) {
                    effect.onWaveStart(currentWave, context);
                }
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context, dt);
        }

        for (int i = context.getZombies().size() - 1; i >= 0; i--) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= GameController.colToWorldX(-1)) {
                context.setGameOver(true);
                context.log("Brain has eaten");
                context.removeZombie(z);
            }
        }
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
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
        context.getGameStats().onPlantPlaced(col, lane, plantCard.getPlant().getType());

        context.removeCard(card);
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
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

        if (availablePlants.isEmpty()) {
            context.log("Conveyor Belt warning: available plants list is empty!");
            return;
        }

        Random random = new Random();
        MyPlant randomPlant = availablePlants.get(random.nextInt(availablePlants.size()));

        PlantCard conveyorCard = new PlantCard(randomPlant, 0, 0);
        context.addCard(conveyorCard);
        context.log("Conveyor delivered a new card: " + randomPlant.getType().toString());
    }
}
