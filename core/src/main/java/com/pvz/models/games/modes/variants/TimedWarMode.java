package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;

import com.pvz.controller.game.GameController;
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

/**
 * Timed War mode. Plays like Normal Mode — waves keep spawning zombies and the
 * player must defend — but carries a single objective: kill a number of zombies
 * (the "target") within any fixed-length time interval (the "window").
 *
 * <p>
 * {@link #outcome} is {@link Outcome#NONE} while the level is running, and
 * becomes {@link Outcome#VICTORY} or {@link Outcome#DEFEAT} once it ends:
 * <ul>
 * <li>{@code VICTORY}: every zombie has been cleared <em>and</em> the objective
 * was completed at some point.</li>
 * <li>{@code DEFEAT}: every zombie has been cleared <em>without</em> ever
 * reaching the target in a single window.</li>
 * </ul>
 */
public class TimedWarMode implements GameMode, PlantPlacer {

    /** The terminal result of a Timed War level. */
    public enum Outcome {
        NONE, VICTORY, DEFEAT
    }

    private Wave currentWave;
    private List<Wave> waves;

    private final int targetKills;
    private final float windowSeconds;

    private float stateTime;

    /**
     * Death time (in seconds since {@code initMode}) of every kill that still
     * falls inside the current sliding window.
     */
    private final List<Float> killedZombieSeconds = new ArrayList<>();
    /** Alive zombies from the previous update, used to detect new kills. */
    private List<Zombie> lastTickAliveZombies = new ArrayList<>();

    private boolean objectiveComplete;
    private Outcome outcome = Outcome.NONE;

    public TimedWarMode(Level level) {
        if (level instanceof TimedWarLevel timedWarLevel) {
            waves = timedWarLevel.getWaves();
            targetKills = timedWarLevel.getTargetKills();
            windowSeconds = timedWarLevel.getWindowSeconds();
        } else {
            targetKills = 5;
            windowSeconds = 10f;
        }
        if (waves != null && !waves.isEmpty()) {
            currentWave = waves.getFirst();
        }
    }

    @Override
    public boolean hasProgressBar() {
        return true;
    }

    @Override
    public int getCurrentWaveIndex() {
        return waves != null ? waves.indexOf(currentWave) : 0;
    }

    @Override
    public int getTotalWaves() {
        return waves != null ? waves.size() : 0;
    }

    @Override
    public int getTotalZombieCount() {
        return waves.stream().mapToInt(Wave::getTotalZombieCount).sum();
    }

    /** True once the objective (target kills inside one window) has been met. */
    public boolean isObjectiveComplete() {
        return objectiveComplete;
    }

    /**
     * Fill amount [0,1] for the HUD objective bar: the number of zombies killed
     * inside the current window divided by the target. Once the objective is met
     * it stays pinned at 1.
     */
    public float getObjectiveProgress() {
        if (objectiveComplete || targetKills <= 0) {
            return 1f;
        }
        float progress = killedZombieSeconds.size() / (float) targetKills;
        return Math.max(0f, Math.min(progress, 1f));
    }

    /** Number of zombies killed inside the current window. */
    public int getKillsInWindow() {
        return killedZombieSeconds.size();
    }

    public int getTargetKills() {
        return targetKills;
    }

    public float getWindowSeconds() {
        return windowSeconds;
    }

    /** Outcome after the level has ended ({@link Outcome#NONE} while running). */
    public Outcome getOutcome() {
        return outcome;
    }

    @Override
    public void initMode(GameContext context) {
        if (currentWave != null) {
            currentWave.startWave(context);
        }
        stateTime = 0f;
        killedZombieSeconds.clear();
        lastTickAliveZombies = new ArrayList<>(context.getZombies());
        objectiveComplete = false;
        outcome = Outcome.NONE;
        context.log("⏱ TIMED WAR MODE STARTED ⏱");
        context.log("Objective: kill " + targetKills + " zombies within any " + windowSeconds + "-second window!");
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        stateTime += dt;
        trackZombieKills(context);
        cleanupExpiredKills(stateTime);
        detectObjectiveCompletion();

        if (updateWaves(context, dt)) {
            return;
        }

        updateLawnMowersAndSuns(context);
    }

    private void trackZombieKills(GameContext context) {
        List<Zombie> currentZombies = context.getZombies();

        for (Zombie last : lastTickAliveZombies) {
            // A zombie that was alive on the previous tick and is now dead was a
            // genuine kill. Zombies that merely leave the field (reached the house)
            // are removed directly without ever becoming dead, so they never reach
            // this branch.
            if (last.isDead()) {
                killedZombieSeconds.add(stateTime);
            }
        }

        lastTickAliveZombies.clear();
        for (Zombie z : currentZombies) {
            if (!z.isDead()) {
                lastTickAliveZombies.add(z);
            }
        }
    }

    /** Drops kills that have fallen outside the sliding window. */
    private void cleanupExpiredKills(float stateTime) {
        killedZombieSeconds.removeIf(deathTime -> (stateTime - deathTime) > windowSeconds);
    }

    private void detectObjectiveCompletion() {
        if (!objectiveComplete && killedZombieSeconds.size() >= targetKills) {
            objectiveComplete = true;
        }
    }

    /**
     * Advances waves like Normal Mode. Returns true (ending the update early) once
     * the whole level is cleared and the win/loss outcome has been decided.
     */
    private boolean updateWaves(GameContext context, float dt) {
        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            if (objectiveComplete) {
                outcome = Outcome.VICTORY;
                context.log(" VICTORY! Objective completed — you defeated every zombie!");
            } else {
                outcome = Outcome.DEFEAT;
                context.log(" GAME OVER! All zombies cleared but you never reached the target kills in one window.");
            }
            context.setGameOver(true);
            return true;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context, dt);
        }
        return false;
    }

    private void updateLawnMowersAndSuns(GameContext context) {
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= GameController.colToWorldX(-1)) {
                context.setGameOver(true);
                context.log("The zombie ate your brain; LOOSER!!!");
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
        if (!(card instanceof PlantCard plantCard)) {
            context.log("Error: card is not a plant card.");
            return;
        }
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (!context.spendSun(stats.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        if (!plant.getAttackAction().isPlantableOnTile(context.getTileAt(col, lane))) {
            return;
        }
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane, plantCard.getPlant().getType());
        plantCard.use();
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
}
