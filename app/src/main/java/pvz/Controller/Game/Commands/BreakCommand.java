package pvz.Controller.Game.Commands;

import pvz.Models.Games.Capabilities.VaseBreaker;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Modes.GameMode;

/**
 * {@code break <col> <lane>} — shatters the vase at the given tile.
 * Only works in modes implementing {@link VaseBreaker} (e.g. {@code VasebreakerMode}).
 */
public class BreakCommand implements GameCommand {

    @Override
    public String getName() {
        return "break";
    }

    @Override
    public String getDescription() {
        return "break <col> <lane> - Shatter the vase at the given tile (Vasebreaker only).";
    }

    @Override
    public void execute(GameContext context, String[] args) throws Exception {
        if (args.length < 2) {
            context.log("Usage: break <col> <lane>");
            return;
        }

        int col = Integer.parseInt(args[0]);
        int lane = Integer.parseInt(args[1]);

        GameMode mode = context.getMode();
        if (mode instanceof VaseBreaker activeBreaker) {
            activeBreaker.breakVase(context, col, lane);
        } else {
            context.log("Error: You cannot break things in this game mode!");
        }
    }
}
