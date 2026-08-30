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
import com.pvz.models.games.effects.ChapterEffect;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.NormalLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class NormalMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;

    public NormalMode(Level level) {
        if (level instanceof NormalLevel normalLevel) {
            waves = normalLevel.getWaves();
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
        if (currentWave != null) {
            currentWave.startWave(context);
            context.getGameStats().onFirstWaveStart(0);
            for (ChapterEffect effect : context.getActiveEffects()) {
                effect.onWaveStart(currentWave, context);
            }
        }
    }

    @Override
    public void updateMode(GameContext context, float dt) {

        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                for (ChapterEffect effect : context.getActiveEffects()) {
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
        if (!plant.getAttackAction().isPlantableOnTile(context.getTileAt(col, lane)))
            return;
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane, plantCard.getPlant().getType());
        plantCard.use();
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
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
