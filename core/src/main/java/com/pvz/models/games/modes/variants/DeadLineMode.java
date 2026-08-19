package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;

import com.pvz.controller.game.GameController;
import com.pvz.enums.AnsiColors;
import com.pvz.models.Constants;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.DeadLineLevel;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;

public class DeadLineMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private int deadlineColumn;
    private static final String CELL_EMPTY = "    ";

    public DeadLineMode(Level level) {
        if (level instanceof DeadLineLevel deadLineLevel) {
            this.waves = deadLineLevel.getWaves();
            this.deadlineColumn = deadLineLevel.getDeadlineColumn();
        }
        if (waves != null && !waves.isEmpty()) {
            currentWave = waves.get(0);
        }
    }

    @Override
    public void initMode(GameContext context) {
        context.log("⚠️ DEADLINE MODE! Don't let zombies cross Column " + deadlineColumn + " ⚠️");
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        if (checkDeadlineLoss(context)) {
            return;
        }

        if (updateWaves(context)) {
            return;
        }

        updateLawnMowersAndSuns(context);
    }

    private boolean checkDeadlineLoss(GameContext context) {
        for (Zombie z : context.getZombies()) {
            if (!z.isDead() && z.getX() <= deadlineColumn) {
                context.setGameOver(true);
                context.log("💥 GAME OVER! A zombie crossed the Dead Line at Column " + deadlineColumn + "!");
                return true;
            }
        }
        return false;
    }

    private boolean updateWaves(GameContext context) {
        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Zombies defeated! You defended the Dead Line successfully!");
            }
            return true;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context, 0);
        }
        return false;
    }

    private void updateLawnMowersAndSuns(GameContext context) {
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= 0f) {
                context.setGameOver(true);
                context.log("Brain has eaten");
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
        if (!(card instanceof PlantCard plantCard)) {
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
        context.spawnPlant(plant);
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
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
