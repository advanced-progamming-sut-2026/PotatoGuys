package pvz.Models.Games.Modes;

import java.util.Arrays;

import pvz.Models.Games.Capabilities.VaseBreaker;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;

/**
 * "Vasebreaker" mini-game skeleton: the board starts covered in vases that
 * hide zombies. Only implements {@link GameMode} + {@link VaseBreaker} — it
 * has no idea {@code PlantPlacer} or {@code BoardSwapper} even exist.
 */
public class VasebreakerMode implements GameMode, VaseBreaker {

    private final boolean[][] vases; // [lane][col]

    public VasebreakerMode(Level level) {
        int lanes = level.getGameMap().getRows();
        int cols = level.getGameMap().getColumns();
        this.vases = new boolean[lanes][cols];
        for (boolean[] row : vases) {
            Arrays.fill(row, true);
        }
    }

    @Override
    public void initMode(GameContext context) {
        context.log("Vasebreaker level started. Break vases to find hidden zombies!");
    }

    @Override
    public void updateMode(GameContext context) {
        if (context.getZombies().isEmpty() && !anyVasesRemain()) {
            context.setGameOver(true);
            context.log("All vases cleared and zombies defeated. Victory!");
        }
    }

    @Override
    public void breakVase(GameContext context, int col, int lane) {
        if (lane < 0 || lane >= vases.length || col < 0 || col >= vases[0].length) {
            context.log("Error: (" + col + ", " + lane + ") is out of bounds.");
            return;
        }
        if (!vases[lane][col]) {
            context.log("There is no vase at (" + col + ", " + lane + ").");
            return;
        }
        vases[lane][col] = false;
        context.log("Vase at (" + col + ", " + lane + ") shattered!");
        // TODO: reveal/spawn whatever the vase contained (zombie, sun, plant, ...).
    }

    private boolean anyVasesRemain() {
        for (boolean[] row : vases) {
            for (boolean hasVase : row) {
                if (hasVase) return true;
            }
        }
        return false;
    }

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder("\n=== Vasebreaker | Tick: ").append(context.getCurrentTick()).append(" ===\n");
        for (int lane = 0; lane < vases.length; lane++) {
            for (int col = 0; col < vases[lane].length; col++) {
                sb.append(vases[lane][col] ? "[V]" : "[ ]");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
    }
}
