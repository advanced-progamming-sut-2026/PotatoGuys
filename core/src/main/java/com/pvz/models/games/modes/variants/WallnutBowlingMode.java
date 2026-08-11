package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pvz.controller.game.GameController;
import com.pvz.enums.AnsiColors;
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
import com.pvz.models.AppContext;

/**
 * Wallnut Bowling mini-game mode.
 * Plants (Wallnut, Explodeonut) are delivered via conveyor belt and can only be
 * placed up to a red deadline line. No sun falls from the sky.
 */
public class WallnutBowlingMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;
    private final int deadlineColumn;

    private int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 10;
    private static final int SPAWN_INTERVAL_TICKS = 6 * TICKS_PER_SECOND;

    private static final List<PlantType> BOWLING_PLANTS = List.of(
            PlantType.Wallnut,
            PlantType.Explodeonut
    );

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
        setupLawnMowers();
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
                if (!lawnMower[GameController.worldYtoLane(z.getY())]) {
                    runLawnMowers(context, GameController.worldYtoLane(z.getY()));
                    i--;
                    continue;
                }
                if (lawnMower[GameController.worldYtoLane(z.getY())]) {
                    context.setGameOver(true);
                    context.log("Brain has eaten!");
                    context.removeZombie(z);
                }
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

    private void setupLawnMowers() {
        int lanes = 5;
        lawnMower = new Boolean[lanes];
        for (int i = 0; i < lanes; i++) {
            lawnMower[i] = false;
        }
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane])
            return;

        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE, true);
            context.removeZombie(zombie);
            context.log("Lawn mower in lane " + lane + " ran over a zombie!");
        });
        lawnMower[lane] = true;
    }

    private static final String CELL_EMPTY = "    ";
    private static final String MOWER_OK = "[M]";
    private static final String MOWER_USED = "[!]";

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n=================================================================================\n");
        sb.append("  MODE: WALLNUT BOWLING  |  RED LINE: Column ").append(deadlineColumn).append("\n");
        sb.append("  Place bowling nuts BEFORE the red line. No sun from the sky!\n");
        sb.append("=================================================================================\n");

        sb.append("\nTick: ").append(context.getCurrentTick())
                .append(" | Zombies: ").append(context.getZombies().size())
                .append(" | Plants: ").append(context.getPlants().size())
                .append(" | Cards: ").append(context.getCards().size())
                .append("\n");

        appendColumnHeaders(sb, context);
        appendDivider(sb, context);
        for (int lane = 0; lane < context.getMap().getLanes(); lane++) {
            appendLaneRow(sb, context, lane);
            appendDivider(sb, context);
        }
        return sb.toString();
    }

    private void appendColumnHeaders(StringBuilder sb, GameContext context) {
        sb.append("\n     ");
        for (int c = 0; c < context.getMap().getColumns(); c++) {
            sb.append(String.format(" C%-2d ", c));
        }
        sb.append("\n");
    }

    private void appendDivider(StringBuilder sb, GameContext context) {
        sb.append("    +");
        for (int c = 0; c < context.getMap().getColumns(); c++) {
            if (c == deadlineColumn - 1) {
                sb.append("----+");
            } else {
                sb.append("----+");
            }
        }
        sb.append("\n");
    }

    private void appendLaneRow(StringBuilder sb, GameContext context, int lane) {
        sb.append(lawnMower[lane] ? MOWER_USED : MOWER_OK).append(" |");
        for (int col = 0; col < context.getMap().getColumns(); col++) {
            sb.append(getCellContent(col, context, lane));
            if (col == deadlineColumn - 1) {
                sb.append(AnsiColors.RED + "║" + AnsiColors.RESET);
            } else {
                sb.append("|");
            }
        }
        sb.append("  Lane ").append(lane).append("\n");
    }

    private String getCellContent(int col, GameContext context, int lane) {
        List<Plant> plantsAtCell = context.getPlantsAt(col, lane);
        boolean hasPlant = !plantsAtCell.isEmpty();

        List<Zombie> zombiesAtCell = context.getZombiesAt(col, lane);
        boolean hasZombie = !zombiesAtCell.isEmpty();

        if (col >= deadlineColumn) {
            if (hasZombie) {
                return String.format(AnsiColors.BRIGHT_RED + " Z%-2d" + AnsiColors.RESET, zombiesAtCell.size());
            }
            return AnsiColors.RED_BG + "    " + AnsiColors.RESET;
        }

        if (hasPlant && hasZombie) {
            return String.format(AnsiColors.GREEN + "P" + AnsiColors.RESET + "/Z%-1d", plantsAtCell.size(),
                    zombiesAtCell.size());
        } else if (hasPlant) {
            String plantSymbol = getPlantDisplaySymbol(plantsAtCell.get(0));
            return String.format(AnsiColors.GREEN + "%s" + AnsiColors.RESET, plantSymbol);
        } else if (hasZombie) {
            return String.format(" Z%-2d", zombiesAtCell.size());
        }
        return CELL_EMPTY;
    }

    private String getPlantDisplaySymbol(Plant plant) {
        return switch (plant.getType()) {
            case Wallnut -> " W  ";
            case Explodeonut -> " E  ";
            default -> " P  ";
        };
    }


}
