package pvz.View.Game;

import pvz.Controller.Game.NormalGameController;
import pvz.Enums.Commands.GameMenuCommand;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Modes.NormalMode;
import pvz.View.Result;

public class NormalGameMenu extends GameMenu {
    private final NormalGameController normalController;
    GameContext context;

    public NormalGameMenu(GameContext context) {
        super(new NormalGameController(context));
        this.context=context;
        this.normalController = (NormalGameController) this.gameController;
    }

    @Override
    public Result handleInput(String input) {
        Result result = super.handleInput(input);
        if (result != null) return result;
        if ((matcher = GameMenuCommand.COLLECT_SUN.getMatcher(input)) != null) return normalController.collectSun(matcher);
        if ((matcher = GameMenuCommand.SHOW_SUN.getMatcher(input)) != null) return normalController.showSun(matcher);
        if ((matcher = GameMenuCommand.CHEAT_SUN.getMatcher(input)) != null) return normalController.cheatSun(matcher);
        if ((matcher = GameMenuCommand.PLANT.getMatcher(input)) != null) return normalController.plant(matcher);
        if ((matcher = GameMenuCommand.PLUCK_PLANT.getMatcher(input)) != null) return normalController.pluckPlant(matcher);
        return new Result("Invalid command in Game.", this);
    }

    @Override
    public Result onEnter() {
        StringBuilder output = new StringBuilder();
        output.append("=== Level ").append(context.getLevelNumber()).append(" ===\n");
        output.append("Sun: ").append(context.getCurrentSun()).append("\n");
        output.append("Plant Food: ").append(context.getPlantFoodCount()).append("/4\n");
        output.append("Waves: ").append(((NormalMode)context.getMode()).getWaves().size()).append("\n");
        output.append("Commands: advance time -t N ticks | show sun amount | show map | feed plant -l (x, y)\n");
        return new Result(output.toString());
    }
}
