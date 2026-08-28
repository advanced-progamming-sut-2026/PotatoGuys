package com.pvz.models.games.modes.variants;

import java.util.Random;

import com.pvz.models.games.GameContext;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.BoardSwapper;

/**
 * "Beghouled" (match-3) mini-game skeleton. Demonstrates a second capability,
 * {@link BoardSwapper}, living side-by-side with {@link VasebreakerMode}'s
 * {@code VaseBreaker} without either mode or the command layer needing to
 * know about the other.
 */
public class BeghouledMode implements GameMode, BoardSwapper {

    private static final int GEM_TYPES = 6;

    private final int[][] board; // [lane][col] -> gem type id

    public BeghouledMode(Level level) {
        int lanes = level.getGameMapDefinition().rows;
        int cols = level.getGameMapDefinition().columns;
        this.board = new int[lanes][cols];
        Random random = new Random();
        for (int[] row : board) {
            for (int c = 0; c < row.length; c++) {
                row[c] = random.nextInt(GEM_TYPES);
            }
        }
    }

    @Override
    public void initMode(GameContext context) {
        context.log("Beghouled started. Swap adjacent gems to make matches of 3+.");
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        // TODO: detect matches, resolve cascades, award sun/damage, check win/loss.
    }

    @Override
    public void swapPlants(GameContext context, int x1, int y1, int x2, int y2) {
        if (!inBounds(x1, y1) || !inBounds(x2, y2)) {
            context.log("Error: swap coordinates out of bounds.");
            return;
        }
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) {
            context.log("Error: can only swap adjacent tiles.");
            return;
        }
        int tmp = board[y1][x1];
        board[y1][x1] = board[y2][x2];
        board[y2][x2] = tmp;
        context.log("Swapped (" + x1 + "," + y1 + ") with (" + x2 + "," + y2 + ").");
        // TODO: if the swap produced no match, swap back.
    }

    private boolean inBounds(int col, int lane) {
        return lane >= 0 && lane < board.length && col >= 0 && col < board[0].length;
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
    }
}
