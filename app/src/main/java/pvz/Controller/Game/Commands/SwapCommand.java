package pvz.Controller.Game.Commands;

import pvz.Models.Games.Capabilities.BoardSwapper;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Modes.GameMode;

/**
 * {@code swap <x1> <y1> <x2> <y2>} — swaps two board cells.
 * Only works in modes implementing {@link BoardSwapper} (e.g. {@code BeghouledMode}).
 */
public class SwapCommand implements GameCommand {

    @Override
    public String getName() {
        return "swap";
    }

    @Override
    public String getDescription() {
        return "swap <x1> <y1> <x2> <y2> - Swap two adjacent board cells (Beghouled only).";
    }

    @Override
    public void execute(GameContext context, String[] args) throws Exception {
        if (args.length < 4) {
            context.log("Usage: swap <x1> <y1> <x2> <y2>");
            return;
        }

        int x1 = Integer.parseInt(args[0]);
        int y1 = Integer.parseInt(args[1]);
        int x2 = Integer.parseInt(args[2]);
        int y2 = Integer.parseInt(args[3]);

        GameMode mode = context.getMode();
        if (mode instanceof BoardSwapper swapper) {
            swapper.swapPlants(context, x1, y1, x2, y2);
        } else {
            context.log("Error: You cannot swap tiles in this game mode!");
        }
    }
}
