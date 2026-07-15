package pvz.Models.Games.Modes;

import java.util.List;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.NormalLevel;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class NormalMode implements GameMode {
    private Wave currentWave;
    private List<Wave> waves;
    private List<PlantCard> plantCards;
    private Boolean[] lawnMower;
    
    public NormalMode(Level level){
        if(level instanceof NormalLevel normalLevel){
            waves = normalLevel.getWaves();
        }
        currentWave = waves.getFirst();
    }
    @Override
    public void initMode(GameContext context) {
        SetupLawnMowers();
    }

    @Override
    public void updateMode(GameContext context) {

        if (context.getZombies().isEmpty()) {
            if (waves.indexOf(currentWave) < waves.size() - 1) {
                currentWave = waves.get(waves.indexOf(currentWave) + 1);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
            return;
        }

        currentWave.updateWave(context);
    }
    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        throw new UnsupportedOperationException("Unimplemented method 'isValidPlacement'");
    }
    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        throw new UnsupportedOperationException("Unimplemented method 'handlePlacement'");
    }

    private void SetupLawnMowers(){
        int lanes = 5; 
        lawnMower = new Boolean[lanes];
        for (int i = 0; i < lanes; i++) {
            lawnMower[i] = true; // All lawn mowers are initially available
        }
    }

    public void runLawnMowers(GameContext context , int lane){   
        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE);
            context.removeZombie(zombie);
            context.log("Lawn mower in lane " + lane + " ran over a zombie!");
        });
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
    public void addPlantCard(PlantCard newCard){
        this.plantCards.add(newCard);
    }


    private static final String CELL_EMPTY = "    ";
    private static final String MOWER_OK   = "[M]";
    private static final String MOWER_USED = "[!]";

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb , context);
        appendColumnHeaders(sb , context);
        appendDivider(sb , context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            appendLaneRow(sb , context, lane);
            appendDivider(sb , context);
        }
        appendDirectionHint(sb , context);
        appendZombieStatus(sb , context);
        return sb.toString();
    }

    // ── Private rendering helpers ─────────────────────────────────────────────

    private void appendHeader(StringBuilder sb , GameContext context) {
        sb.append("\n=== Tick: ").append(context.getCurrentTick())
          .append(" | Sun: ").append(context.getSuns().size())
          .append(" | Active zombies: ").append(context.getZombies().size()) // Example for lane 0
          .append(" ===\n");
    }

    private void appendColumnHeaders(StringBuilder sb , GameContext context) {
        sb.append("        ");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append(String.format("  C%-2d ", c));
        }
        sb.append("\n");
    }

    private void appendDivider(StringBuilder sb , GameContext context) {
        sb.append("    +");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append("----+");
        }
        sb.append("\n");
    }

    private void appendLaneRow(StringBuilder sb, GameContext context , int lane) {
        sb.append(lawnMower[lane] ? MOWER_USED : MOWER_OK).append(" |");
        for (int col = 0; col < context.getColumns(); col++) {
            sb.append(getCellContent(col, context, lane)).append('|');
        }
        sb.append("  Lane ").append(lane).append("\n");
    }

    private void appendDirectionHint(StringBuilder sb , GameContext context) {
        sb.append("         ←←←←←←← zombies walk this direction\n");
    }

    private void appendZombieStatus(StringBuilder sb , GameContext context) {
        if (context.getZombies().isEmpty()) { sb.append("\n(no zombies)\n"); return; }
        sb.append("\nZombies (").append(context.getZombies().size()).append(" active):\n");
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (!z.isDead()) {
                sb.append("  Z").append(i).append(" ").append(z.toInfoString()).append("\n");
            }
        }
    }

    /** Returns 4-char cell content for rendering, e.g. {@code " Z0 "} or {@code "    "}. */
    private String getCellContent(int col , GameContext context, int lane) {
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (!z.isDead() && z.getLane() == lane && (int) z.getX() == col) {
                return String.format("Z%-3d", i);
            }
        }
        return CELL_EMPTY;
    }
}
