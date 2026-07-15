package pvz.View.Game;

import pvz.Controller.Game.NormalGameController;
import pvz.Enums.Commands.GameMenuCommand;
import pvz.Models.toDel.Seasons.Levels.LevelGameContext;
import pvz.Models.toDel.Seasons.Levels.NormalLevel;
import pvz.Models.toDel.Seasons.Levels.Wave;
import pvz.View.Result;

public class NormalGameMenu extends GameMenu {
    private final NormalGameController normalController;
    private final NormalLevel level;
    private boolean wavesStarted;

    public NormalGameMenu(NormalLevel level) {
        super(new NormalGameController(level));
        this.level = level;
        this.normalController = (NormalGameController) this.gameController;
        this.wavesStarted = false;
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
        output.append("=== Level ").append(level.getLevelNumber()).append(" ===\n");
        output.append("Sun: ").append(level.getCurrentSun()).append("\n");
        output.append("Waves: ").append(level.getWaves().size()).append("\n");
        output.append("Commands: advance time -t N ticks | show sun amount | show map | zombies info\n");

        if (!wavesStarted) {
            Wave first = level.getCurrentWave();
            if (first != null) {
                level.getEngine().register(first);
                wavesStarted = true;
                output.append("\nWave ").append(first.getWaveNumber()).append(" incoming...");
            }
        }
        return new Result(output.toString());
    }
}
