package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;

import com.pvz.controller.game.GameController;
import com.pvz.enums.AnsiColors;
import com.pvz.models.Constants;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.plants.grave_buster.GraveBuster;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.effects.ChapterEffect;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.NormalLevel;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.view.game.GameScreen;

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
    public void initMode(GameContext context) {
        if (currentWave != null) {
            currentWave.startWave(context);
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

        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
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
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (!context.spendSun(stats.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        if (!plant.getAttackAction().isPlantableOnTile(context.getTileAt(col,lane))) return;
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

    @Override
    public String getCardsStatus(GameContext context) {
        StringBuilder result = new StringBuilder();
        List<Card> cards = context.getCards();

        if (cards == null || cards.isEmpty()) {
            result.append("No plant cards available.");
        } else {
            result.append("=== SEED PACKETS ===");

            int cardWidth = 40;

            for (int i = 0; i < cards.size(); i++) {
                PlantCard ps = (PlantCard) cards.get(i);

                String cardInfo = String.format("- %s | Cost:%d | Lvl:%d | Cooldown:%.1f%s",
                    ps.getPlant().getType(),
                    ps.getCost(),
                    ps.getPlant().getLevel(),
                    (float) ps.getCooldown() / (float) Constants.TICK_PER_SECOND,
                    ps.getPlant().isBoosted() ? " | ⚡B" : "");

                result.append("\n");
                result.append(String.format("%-" + cardWidth + "s", cardInfo));
            }
        }
        return result.toString();
    }
}
