package pvz.Models.Games.Modes.variants;

import java.util.List;
import java.util.Random;

import pvz.Models.AppContext;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Levels.variants.ConveyorBeltLevel;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.User.MyPlant;

/**
 * Standard game mode implementation for Conveyor Belt level.
 * Automatically spawns random unlocked plants every 12 seconds.
 */
public class ConveyorBeltMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;

    private int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 10;
    private static final int SPAWN_INTERVAL_TICKS = 12 * TICKS_PER_SECOND;

    public ConveyorBeltMode(Level level) {
        if (level instanceof ConveyorBeltLevel normalLevel) {
            waves = normalLevel.getWaves();
        }else{
            return;
        }
        currentWave = waves.get(0);
        SetupLawnMowers();
    }

    @Override
    public void initMode(GameContext context) {
        addRandomCard(context);
        tickCounter = 0;
    }

    @Override
    public void updateMode(GameContext context) {
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
            currentWave.updateWave(context);
        }

        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);

            if (z.getX() <= 0f) {
                if (!lawnMower[z.getLane()]) {
                    runLawnMowers(context, z.getLane());
                    i--;
                    continue;
                }
                if (lawnMower[z.getLane()]) {
                    context.setGameOver(true);
                    context.log("Brain has eaten");
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
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
            return false;
        }
        if (!context.getPlantsAt(col, lane).isEmpty()) {
            return false;
        }
        if (card == null || !(card instanceof PlantCard)) {
            return false;
        }
        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (!(card instanceof PlantCard plantCard)) {
            context.log("Error: card is not a plant card.");
            return;
        }

        Plant plant = new PlantFactory().create(
                plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context
        );
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
        if (currentUser == null || currentUser.getProfile() == null || currentUser.getProfile().getCollection() == null) {
            context.log("Error: User profiles or collection is not loaded.");
            return;
        }

        List<MyPlant> unlockedPlants = currentUser.getProfile().getCollection().getUnlockedPlants();
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

    private void SetupLawnMowers() {
        int lanes = 5;
        lawnMower = new Boolean[lanes];
        for (int i = 0; i < lanes; i++) {
            lawnMower[i] = false;
        }
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane]) return;

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
        appendHeader(sb, context);
        appendColumnHeaders(sb, context);
        appendDivider(sb, context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            appendLaneRow(sb, context, lane);
            appendDivider(sb, context);
        }
        return sb.toString();
    }

    private void appendHeader(StringBuilder sb, GameContext context) {
        sb.append("\n=== Tick: ").append(context.getCurrentTick())
                .append(" | Sun: ").append(context.getCurrentSun())
                .append(" | zombies: ").append(context.getZombies().size())
                .append(" | plants: ").append(context.getPlants().size())
                .append(" | projectiles: ").append(context.getProjectiles().size())
                .append(" | suns: ").append(context.getSuns().size())
                .append(" ===\n");
    }

    private void appendColumnHeaders(StringBuilder sb, GameContext context) {
        sb.append("\n     ");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append(String.format(" C%-2d ", c));
        }
        sb.append("\n");
    }

    private void appendDivider(StringBuilder sb, GameContext context) {
        sb.append("    +");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append("----+");
        }
        sb.append("\n");
    }

    private void appendLaneRow(StringBuilder sb, GameContext context, int lane) {
        sb.append(lawnMower[lane] ? MOWER_USED : MOWER_OK).append(" |");
        for (int col = 0; col < context.getColumns(); col++) {
            sb.append(getCellContent(col, context, lane)).append('|');
        }
        sb.append("  Lane ").append(lane).append("\n");
    }

    private String getCellContent(int col, GameContext context, int lane) {
        List<Plant> plantsAtCell = context.getPlantsAt(col, lane);
        boolean hasPlant = !plantsAtCell.isEmpty();

        List<Zombie> zombiesAtCell = context.getZombiesAt(col, lane);
        boolean hasZombie = !zombiesAtCell.isEmpty();

        if (hasPlant && hasZombie) {
            return String.format("P/Z%-1d", plantsAtCell.size(), zombiesAtCell.size());
        } else if (hasPlant) {
            return String.format(" P  ", plantsAtCell.size());
        } else if (hasZombie) {
            return String.format(" Z%-2d", zombiesAtCell.size());
        }
        return CELL_EMPTY;
    }
}