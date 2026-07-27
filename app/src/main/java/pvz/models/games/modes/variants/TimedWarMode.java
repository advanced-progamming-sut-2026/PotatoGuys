package pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;

import pvz.models.Constants;
import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.PlantFactory;
import pvz.models.entities.plants.data.PlantPropertySheet;
import pvz.models.entities.plants.data.PlantRegistry;
import pvz.models.entities.plants.data.PlantStatResolver;
import pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import pvz.models.entities.sun.Sun;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;
import pvz.models.games.card.Card;
import pvz.models.games.card.PlantCard;
import pvz.models.games.levels.Level;
import pvz.models.games.levels.Wave;
import pvz.models.games.levels.variants.TimedWarLevel;
import pvz.models.games.modes.GameMode;
import pvz.models.games.modes.capabilities.PlantPlacer;

public class TimedWarMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;

    private static final int TICKS_PER_SECOND = 10;
    private static final int WINDOW_SECONDS = 8;
    private static final int TARGET_KILLS = 5;
    private static final int TOTAL_TIME_LIMIT_SECONDS = 60;

    private final List<Integer> killedZombieTicks = new ArrayList<>();
    private List<Zombie> lastTickZombies = new ArrayList<>();

    public TimedWarMode(Level level) {
        if (level instanceof TimedWarLevel timedWarLevel) {
            waves = timedWarLevel.getWaves();
        }
        currentWave = waves.getFirst();
        setupLawnMowers();
    }

    @Override
    public void initMode(GameContext context) {
        context.log("⏱ TIMED WAR MODE STARTED ⏱");
        context.log("Objective: Kill " + TARGET_KILLS + " zombies within any " + WINDOW_SECONDS + "-second window!");
        lastTickZombies = new ArrayList<>(context.getZombies());
    }

    @Override
    public void updateMode(GameContext context) {
        int currentTick = context.getCurrentTick();
        trackZombieKills(context);
        cleanupExpiredKills(currentTick);

        if (checkVictoryCondition(context)) {
            return;
        }

        if (checkGameOverCondition(context, currentTick)) {
            return;
        }

        updateWaveAndEntities(context, currentTick);
    }

    private void trackZombieKills(GameContext context) {
        int currentTick = context.getCurrentTick();
        List<Zombie> currentZombies = context.getZombies();
        for (Zombie oldZombie : lastTickZombies) {
            if (!currentZombies.contains(oldZombie)) {
                if (oldZombie.getX() > 0f) {
                    killedZombieTicks.add(currentTick);
                }
            }
        }
        lastTickZombies = new ArrayList<>(currentZombies);
    }

    private void cleanupExpiredKills(int currentTick) {
        int windowTicks = WINDOW_SECONDS * TICKS_PER_SECOND;
        killedZombieTicks.removeIf(deathTick -> (currentTick - deathTick) > windowTicks);
    }

    private boolean checkVictoryCondition(GameContext context) {
        if (killedZombieTicks.size() >= TARGET_KILLS) {
            context.setGameOver(true);
            context.log(" VICTORY! You successfully killed " + TARGET_KILLS + " zombies in a " + WINDOW_SECONDS
                    + " second window!");
            return true;
        }
        return false;
    }

    private boolean checkGameOverCondition(GameContext context, int currentTick) {
        if (currentTick >= TOTAL_TIME_LIMIT_SECONDS * TICKS_PER_SECOND) {
            context.setGameOver(true);
            context.log(" GAME OVER! Time ran out. You failed to reach the target kill streak.");
            return true;
        }
        return false;
    }

    private void updateWaveAndEntities(GameContext context, int currentTick) {
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
            currentWave.updateWave(context);
        }

        updateLawnMowersAndZombies(context);
        updateSuns(context);
    }

    private void updateLawnMowersAndZombies(GameContext context) {
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
                    context.log("The zombie ate your brain; LOOSER!!!");
                    context.removeZombie(z);
                }
            }
        }
    }

    private void updateSuns(GameContext context) {
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

        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
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
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        if (!context.spendSun(sheet.getSunCost())) {
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

    @Override
    public String getCardsStatus(GameContext context) {
        StringBuilder result = new StringBuilder();
        List<Card> cards = context.getCards();
        if (cards == null || cards.isEmpty()) {
            result.append("No plant cards available.");
        } else {
            result.append("=== SEED PACKETS ===");
            int cardWidth = 40;
            for (Card card : cards) {
                PlantCard ps = (PlantCard) card;
                String cardInfo = String.format("- %s | Cost:%d | Lvl:%d | Cooldown:%.1f%s",
                        ps.getPlant().getType(), ps.getCost(), ps.getPlant().getLevel(),
                        (float) ps.getCooldown() / (float) Constants.TICK_PER_SECOND,
                        ps.getPlant().isBoosted() ? " | ⚡B" : "");
                result.append("\n").append(String.format("%-" + cardWidth + "s", cardInfo));
            }
        }
        return result.toString();
    }

    private void setupLawnMowers() {
        lawnMower = new Boolean[5];
        for (int i = 0; i < 5; i++) {
            lawnMower[i] = false;
        }
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane]) {
            return;
        }
        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE);
            context.removeZombie(zombie);
        });
        context.log("Lawn mower in lane " + lane + " ran over zombies!");
        lawnMower[lane] = true;
    }

    private static final String CELL_EMPTY = "    ";
    private static final String MOWER_OK = "[M]";
    private static final String MOWER_USED = "[!]";

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();
        appendTimedWarHeader(sb, context);
        appendColumnHeaders(sb, context);
        appendDivider(sb, context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            appendLaneRow(sb, context, lane);
            appendDivider(sb, context);
        }
        return sb.toString();
    }

    private void appendTimedWarHeader(StringBuilder sb, GameContext context) {
        double totalTimeRemaining = Math.max(0,
                TOTAL_TIME_LIMIT_SECONDS - ((double) context.getCurrentTick() / TICKS_PER_SECOND));
        int currentKillsInWindow = killedZombieTicks.size();

        sb.append("\n=================================================================================\n");
        sb.append(String.format("  TIMED WAR MODE  |  Time Remaining: %.1f seconds\n", totalTimeRemaining));
        sb.append(String.format("  OBJECTIVE: Kill %d zombies within a %d-second rolling window\n", TARGET_KILLS,
                WINDOW_SECONDS));
        sb.append(String.format("  CURRENT STREAK (Last %d seconds): %d / %d zombies\n", WINDOW_SECONDS,
                currentKillsInWindow, TARGET_KILLS));
        sb.append("=================================================================================\n");

        sb.append("Tick: ").append(context.getCurrentTick())
                .append(" | Sun: ").append(context.getCurrentSun())
                .append(" | Zombies Alive: ").append(context.getZombies().size())
                .append(" | Plants: ").append(context.getPlants().size())
                .append("\n");
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
            return String.format("P/Z%-1d", plantsAtCell.size());
        } else if (hasPlant) {
            return " P  ";
        } else if (hasZombie) {
            return String.format(" Z%-2d", zombiesAtCell.size());
        }
        return CELL_EMPTY;
    }
}