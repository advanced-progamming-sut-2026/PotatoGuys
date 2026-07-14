package pvz.View.Game;

import java.util.List;
import java.util.regex.Matcher;

import pvz.Controller.Game.GameController;
import pvz.Enums.Commands.GameMenuCommand;
import pvz.Models.Entities.Plants.Plant;
import pvz.View.Menu;
import pvz.View.Result;

public abstract class GameMenu implements Menu {
    GameController gameController;
    Matcher matcher;

    public GameMenu(GameController gameController){
        super();
        this.gameController=gameController;
    }

    @Override
    public Result handleInput(String input) {
        if ((matcher = GameMenuCommand.ADVANCE_TIME.getMatcher(input)) != null) return gameController.advanceTime(matcher);
        if ((matcher = GameMenuCommand.RELEASE_NUKE.getMatcher(input)) != null) return gameController.releaseNuke(matcher);
        if ((matcher = GameMenuCommand.CHEAT_COOLDOWN.getMatcher(input)) != null) return gameController.cheatCooldown(matcher);
        if ((matcher = GameMenuCommand.FEED_PLANT.getMatcher(input)) != null) return gameController.feedPlant(matcher);
        if ((matcher = GameMenuCommand.CHEAT_PLANT_FOOD.getMatcher(input)) != null) return gameController.cheatPlantFood(matcher);
        if ((matcher = GameMenuCommand.SHOW_MAP.getMatcher(input)) != null) return gameController.showMap(matcher);
        if ((matcher = GameMenuCommand.SHOW_PLANTS_STATUS.getMatcher(input)) != null) return gameController.showPlantsStatus(matcher);
        if ((matcher = GameMenuCommand.SHOW_TILE_STATUS.getMatcher(input)) != null) return gameController.showTileStatus(matcher);
        if ((matcher = GameMenuCommand.ZOMBIES_INFO.getMatcher(input)) != null) return gameController.zombiesInfo(matcher);
        if ((matcher = GameMenuCommand.CHEAT_SPAWN_ZOMBIE.getMatcher(input)) != null) return gameController.cheatSpawnZombie(matcher);
        return null;
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
