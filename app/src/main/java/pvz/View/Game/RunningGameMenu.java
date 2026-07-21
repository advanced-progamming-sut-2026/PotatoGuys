package pvz.View.Game;

import java.util.regex.Matcher;

import pvz.Controller.Game.GameController;
import pvz.Enums.Commands.RunningGameCommand;
import pvz.View.Menu;
import pvz.View.Result;

public class RunningGameMenu implements Menu {
    GameController gameController;
    Matcher matcher;

    public RunningGameMenu(GameController gameController){
        this.matcher = null;
        this.gameController = gameController;
    }

    @Override
    public Result handleInput(String input) {
        if ((matcher = RunningGameCommand.ADVANCE_TIME.getMatcher(input)) != null) return gameController.advanceTime(matcher);
        if ((matcher = RunningGameCommand.RELEASE_NUKE.getMatcher(input)) != null) return gameController.releaseNuke(matcher);
        if ((matcher = RunningGameCommand.CHEAT_COOLDOWN.getMatcher(input)) != null) return gameController.cheatCooldown(matcher);
        if ((matcher = RunningGameCommand.FEED_PLANT.getMatcher(input)) != null) return gameController.feedPlant(matcher);
        if ((matcher = RunningGameCommand.CHEAT_PLANT_FOOD.getMatcher(input)) != null) return gameController.cheatPlantFood(matcher);
        if ((matcher = RunningGameCommand.SHOW_MAP.getMatcher(input)) != null) return gameController.showMap(matcher);
        if ((matcher = RunningGameCommand.SHOW_PLANTS.getMatcher(input)) != null) return gameController.showPlants(matcher);
        if ((matcher = RunningGameCommand.SHOW_ZOMBIES.getMatcher(input)) != null) return gameController.showZombies(matcher);
        if ((matcher = RunningGameCommand.SHOW_PROJECTILE.getMatcher(input)) != null) return gameController.showProjectiles(matcher);
        if ((matcher = RunningGameCommand.SHOW_SUNS.getMatcher(input)) != null) return gameController.showSuns(matcher);
        if ((matcher = RunningGameCommand.SHOW_TILE_STATUS.getMatcher(input)) != null) return gameController.showTileStatus(matcher);
        if ((matcher = RunningGameCommand.CHEAT_SPAWN_ZOMBIE.getMatcher(input)) != null) return gameController.cheatSpawnZombie(matcher);
        if ((matcher = RunningGameCommand.COLLECT_SUN.getMatcher(input)) != null) return gameController.collectSun(matcher);
        if ((matcher = RunningGameCommand.SHOW_SUN.getMatcher(input)) != null) return gameController.showSun(matcher);
        if ((matcher = RunningGameCommand.CHEAT_SUN.getMatcher(input)) != null) return gameController.cheatSun(matcher);
        if ((matcher = RunningGameCommand.SHOW_CARDS.getMatcher(input)) != null) return gameController.showCards(matcher);
        if ((matcher = RunningGameCommand.START_ZOMBIE_WAVE.getMatcher(input)) != null) return gameController.startZombieWavesCommand(matcher);
        if ((matcher = RunningGameCommand.PLANT.getMatcher(input)) != null) return gameController.plantPlant(matcher);
        if ((matcher = RunningGameCommand.PLUCK_PLANT.getMatcher(input)) != null) return gameController.pluckPlant(matcher);
        if ((matcher = RunningGameCommand.HELP.getMatcher(input)) != null) return new Result(RunningGameCommand.getHelp());
        return new Result("Command not found!");
    }

    @Override
    public String getName(){
        return "Game menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
