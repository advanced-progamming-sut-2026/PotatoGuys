package pvz.Controller.Game.Commands;

import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.card.Card;

/**
 * {@code plant <type> <col> <lane>} — places a seed-packet card on the board.
 * Only works in modes implementing {@link PlantPlacer} (e.g. {@code NormalMode}).
 */
public class PlantCommand implements GameCommand {

    @Override
    public String getName() {
        return "plant";
    }

    @Override
    public String getDescription() {
        return "plant <type> <col> <lane> - Place a plant card on the board.";
    }

    @Override
    public void execute(GameContext context, String[] args) throws Exception {
        if (args.length < 3) {
            context.log("Usage: plant <type> <col> <lane>");
            return;
        }

        String plantType = args[0];
        int col = Integer.parseInt(args[1]);
        int lane = Integer.parseInt(args[2]);

        
    }
}
