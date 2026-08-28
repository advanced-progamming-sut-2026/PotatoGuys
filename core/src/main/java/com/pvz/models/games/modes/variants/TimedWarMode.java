package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.TimedWarLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;

public class TimedWarMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;

    private static final int WINDOW_SECONDS = 8;
    private static final int TARGET_KILLS = 5;
    private static final int TOTAL_TIME_LIMIT_SECONDS = 60;

    float stateTime;

    private final List<Float> killedZombieSeconds = new ArrayList<>();
    private List<Zombie> lastTickZombies = new ArrayList<>();

    public TimedWarMode(Level level) {
        if (level instanceof TimedWarLevel timedWarLevel) {
            waves = timedWarLevel.getWaves();
        }
        currentWave = waves.getFirst();
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
        context.log("⏱ TIMED WAR MODE STARTED ⏱");
        context.log("Objective: Kill " + TARGET_KILLS + " zombies within any " + WINDOW_SECONDS + "-second window!");
        lastTickZombies = new ArrayList<>(context.getZombies());
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        stateTime += dt;
        trackZombieKills(context);
        cleanupExpiredKills(stateTime);

        if (checkVictoryCondition(context)) {
            return;
        }

        if (checkGameOverCondition(context, stateTime)) {
            return;
        }

        updateWaveAndEntities(context, stateTime);
    }

    private void trackZombieKills(GameContext context) {
        List<Zombie> currentZombies = context.getZombies();
        for (Zombie oldZombie : lastTickZombies) {
            if (!currentZombies.contains(oldZombie)) {
                if (oldZombie.getX() > 0f) {
                    killedZombieSeconds.add(stateTime);
                }
            }
        }
        lastTickZombies = new ArrayList<>(currentZombies);
    }

    private void cleanupExpiredKills(float stateTime) {
        killedZombieSeconds.removeIf(deathTime -> (stateTime - deathTime) > WINDOW_SECONDS);
    }

    private boolean checkVictoryCondition(GameContext context) {
        if (killedZombieSeconds.size() >= TARGET_KILLS) {
            context.setGameOver(true);
            context.log(" VICTORY! You successfully killed " + TARGET_KILLS + " zombies in a " + WINDOW_SECONDS
                    + " second window!");
            return true;
        }
        return false;
    }

    private boolean checkGameOverCondition(GameContext context, float stateTime) {
        if (stateTime >= TOTAL_TIME_LIMIT_SECONDS) {
            context.setGameOver(true);
            context.log(" GAME OVER! Time ran out. You failed to reach the target kill streak.");
            return true;
        }
        return false;
    }

    private void updateWaveAndEntities(GameContext context, float stateTime) {
        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context, 0);
        }

        updateLawnMowersAndZombies(context);
        updateSuns(context);
    }

    private void updateLawnMowersAndZombies(GameContext context) {
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= 0f) {
                context.setGameOver(true);
                context.log("The zombie ate your brain; LOOSER!!!");
                context.removeZombie(z);
            }
        }
    }

    private void updateSuns(GameContext context) {
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

        if (!context.getPlantsAt(col, lane).isEmpty()) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") is already occupied by another plant.");
            return false;
        }

        if (!context.getTileAt(col, lane).isPlantable(card)) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") does not support planting "
                    + card.getPlant().getType());
            return false;
        }

        if (!card.canUse()) {
            context.log("[Placement Failed] Card " + card.getPlant().getType() + " is on cooldown or locked.");
            return false;
        }

        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());

        if (context.getCurrentSun() < stats.getSunCost()) {
            context.log("[Placement Failed] Not enough sun for " + sheet.getName()
                    + "! Required: " + stats.getSunCost() + ", Current: " + context.getCurrentSun());
            return false;
        }

        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (!context.spendSun(stats.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(card.getPlant().getType(), col, lane,
                card.getPlant().getLevel(), card.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane, card.getPlant().getType());
        card.use();
        context.log(card.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
    }

    @Override
    public PlantCard findCard(GameContext context, String plantType) {
        for (Card card : context.getCards()) {
            PlantCard plantCard = (PlantCard) card;
            if (plantCard.getPlant().getType().toString().equalsIgnoreCase(plantType)) {
                return plantCard;
            }
        }
        return null;
    }
}
