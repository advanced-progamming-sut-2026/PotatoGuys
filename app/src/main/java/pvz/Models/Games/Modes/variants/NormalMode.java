package pvz.Models.Games.Modes.variants;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Levels.variants.NormalLevel;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.Games.map.Tile;
import pvz.Models.Games.map.TileTags;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class NormalMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;

    public NormalMode(Level level) {
        if (level instanceof NormalLevel normalLevel) {
            waves = normalLevel.getWaves();
        }
        currentWave = waves.getFirst();
        SetupLawnMowers();
    }

    @Override
    public void initMode(GameContext context) {

    }

    @Override
    public void updateMode(GameContext context) {

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
                    context.log("The zombie ate your brain; LOOSER!!!");
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
        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
            return false;
        }
        if (!context.getPlantsAt(col, lane).isEmpty()) {
            return false;
        }
        if (!context.getTileAt(col, lane).isPlantable(card)) {
            return false;
        }
        if (card == null || !card.canUse()) {
            return false;
        }

        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        return context.getCurrentSun() >= sheet.getSunCost();
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        if (!(card instanceof PlantCard plantCard)) {
            context.log("Error: card is not a plant card.");
            return;
        }
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(plantCard.getPlant().getType());
        if (!context.spendSun(sheet.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane);
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
                        ps.getCooldown(),
                        ps.getPlant().isBoosted() ? " | ⚡B" : ""
                );

                result.append("\n");
                result.append(String.format("%-" + cardWidth + "s", cardInfo));
            }
        }
        return result.toString();
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
            zombie.takeDamage(Float.MAX_VALUE);
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

    // ── Private rendering helpers ─────────────────────────────────────────────

    private void appendHeader(StringBuilder sb, GameContext context) {
        sb.append("\n=== Tick: ").append(context.getCurrentTick())
                .append(" | Sun: ").append(context.getCurrentSun())
                .append(" | Zombies: ").append(context.getZombies().size())
                .append(" | Plants: ").append(context.getPlants().size())
                .append(" | Projectiles: ").append(context.getProjectiles().size())
                .append(" | Suns: ").append(context.getSuns().size())
                .append(" | Plant foods: ").append(context.getPlantFoodCount())
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

        Tile tile = context.getTileAt(col, lane);
        if (tile!=null){
            if (tile.getTags().contains(TileTags.GRAVE)){
                if (hasZombie) return String.format("G/Z%-1d",zombiesAtCell.size());
                return " G  ";
            }
        }

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
