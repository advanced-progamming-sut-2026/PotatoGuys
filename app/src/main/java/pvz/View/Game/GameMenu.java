package pvz.View.Game;

import java.util.regex.Matcher;

import pvz.Controller.Game.GameController;
import pvz.Enums.Commands.GameMenuCommand;
import pvz.View.Menu;
import pvz.View.Result;

public class GameMenu implements Menu {
    GameController gameController;
    Matcher matcher;

    public GameMenu(GameController gameController){
        this.matcher = null;
        this.gameController = gameController;
    }

    @Override
    public Result handleInput(String input) {
        if ((matcher = GameMenuCommand.ADVANCE_TIME.getMatcher(input)) != null) return gameController.advanceTime(matcher);
        if ((matcher = GameMenuCommand.RELEASE_NUKE.getMatcher(input)) != null) return gameController.releaseNuke(matcher);
        if ((matcher = GameMenuCommand.CHEAT_COOLDOWN.getMatcher(input)) != null) return gameController.cheatCooldown(matcher);
        if ((matcher = GameMenuCommand.FEED_PLANT.getMatcher(input)) != null) return gameController.feedPlant(matcher);
        if ((matcher = GameMenuCommand.CHEAT_PLANT_FOOD.getMatcher(input)) != null) return gameController.cheatPlantFood(matcher);
        if ((matcher = GameMenuCommand.SHOW_MAP.getMatcher(input)) != null) return gameController.showMap(matcher);
        if ((matcher = GameMenuCommand.SHOW_PLANTS.getMatcher(input)) != null) return gameController.showPlants(matcher);
        if ((matcher = GameMenuCommand.SHOW_ZOMBIES.getMatcher(input)) != null) return gameController.showZombies(matcher);
        if ((matcher = GameMenuCommand.SHOW_PROJECTILE.getMatcher(input)) != null) return gameController.showProjectiles(matcher);
        if ((matcher = GameMenuCommand.SHOW_SUNS.getMatcher(input)) != null) return gameController.showSuns(matcher);
        if ((matcher = GameMenuCommand.SHOW_TILE_STATUS.getMatcher(input)) != null) return gameController.showTileStatus(matcher);
        if ((matcher = GameMenuCommand.CHEAT_SPAWN_ZOMBIE.getMatcher(input)) != null) return gameController.cheatSpawnZombie(matcher);
        if ((matcher = GameMenuCommand.COLLECT_SUN.getMatcher(input)) != null) return gameController.collectSun(matcher);
        if ((matcher = GameMenuCommand.SHOW_SUN.getMatcher(input)) != null) return gameController.showSun(matcher);
        if ((matcher = GameMenuCommand.CHEAT_SUN.getMatcher(input)) != null) return gameController.cheatSun(matcher);
        if ((matcher = GameMenuCommand.SHOW_CARDS.getMatcher(input)) != null) return gameController.showCards(matcher);
        if ((matcher = GameMenuCommand.START_ZOMBIE_WAVE.getMatcher(input)) != null) return gameController.startZombieWavesCommand(matcher);
        if ((matcher = GameMenuCommand.PLANT.getMatcher(input)) != null) return gameController.plantPlant(matcher);
        if ((matcher = GameMenuCommand.PLUCK_PLANT.getMatcher(input)) != null) return gameController.pluckPlant(matcher);
        if ((matcher = GameMenuCommand.HELP.getMatcher(input)) != null) return new Result(GameMenuCommand.getHelp());
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
