package pvz.Models.Games.Modes;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.NormalLevel;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class ConveyorBeltMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private List<PlantCard> plantCards;
    private Boolean[] lawnMower;

    public ConveyorBeltMode(Level level) {
        if (level instanceof NormalLevel normalLevel) {
            waves = normalLevel.getWaves();
        }
        currentWave = waves.get(0);
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
                    context.log("Brain has eaten");
                    context.removeZombie(z);
                }
            }
        }
        for (int i = 0; i < context.getSuns().size(); i++) {
            Sun sun=context.getSuns().get(i);
            if (sun.isDone()){
                context.removeSun(sun);
                i--;
            }
        }
    }


    public Card findCard(String plantType) {
        for (PlantCard card : plantCards) {
            if (card.getPlant().getType().toString().equalsIgnoreCase(plantType)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
            return false;
        }
        if (!context.getPlantsAt(col, lane).isEmpty()) {
            return false;
        }
        if (card == null) {
            return false;
        }
        if (card instanceof PlantCard) {
            return true;
        }
        return false;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (!(card instanceof PlantCard plantCard)) {
            context.log("Error: card is not a plant card.");
            return;
        }

        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
    }

    @Override
    public String getCardsStatus(GameContext context) {
        throw new UnsupportedOperationException("Unimplemented method 'getCardsStatus'");
    }

    private void SetupLawnMowers() {
        int lanes = 5;
        lawnMower = new Boolean[lanes];
        for (int i = 0; i < lanes; i++) {
            lawnMower[i] = false; // All lawn mowers are initially available
        }
    }

    public void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane]) return; // Already used

        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE);
            context.removeZombie(zombie);
            context.log("Lawn mower in lane " + lane + " ran over a zombie!");
        });
        lawnMower[lane] = true;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public void setWaves(List<Wave> waves) {
        this.waves = waves;
    }

    public List<PlantCard> getPlantCards() {
        return plantCards;
    }

    public void setPlantCards(List<PlantCard> plantCards) {
        this.plantCards = plantCards;
    }

    public void addPlantCard(PlantCard newCard) {
        this.plantCards.add(newCard);
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
        // appendDirectionHint(sb , context);
        // appendZombieStatus(sb , context);
        return sb.toString();
    }

    // ── Private rendering helpers ─────────────────────────────────────────────

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

    @Override
    public PlantCard findCard(GameContext context, String plantType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findCard'");
    }
}
