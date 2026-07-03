package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.GameController;
import pvz.Enums.Commands.GameMenuCommand;

public class GameMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        GameController controller=new GameController();
        Matcher matcher;
        if ((matcher = GameMenuCommand.ADVANCE_TIME.getMatcher(input)) != null) return controller.advanceTime(matcher);
        if ((matcher = GameMenuCommand.COLLECT_SUN.getMatcher(input)) != null) return controller.collectSun(matcher);
        if ((matcher = GameMenuCommand.SHOW_SUN.getMatcher(input)) != null) return controller.showSun(matcher);
        if ((matcher = GameMenuCommand.CHEAT_SUN.getMatcher(input)) != null) return controller.cheatSun(matcher);
        if ((matcher = GameMenuCommand.RELEASE_NUKE.getMatcher(input)) != null) return controller.releaseNuke(matcher);
        if ((matcher = GameMenuCommand.PLANT.getMatcher(input)) != null) return controller.plant(matcher);
        if ((matcher = GameMenuCommand.CHEAT_COOLDOWN.getMatcher(input)) != null) return controller.cheatCooldown(matcher);
        if ((matcher = GameMenuCommand.PLUCK_PLANT.getMatcher(input)) != null) return controller.pluckPlant(matcher);
        if ((matcher = GameMenuCommand.FEED_PLANT.getMatcher(input)) != null) return controller.feedPlant(matcher);
        if ((matcher = GameMenuCommand.CHEAT_PLANT_FOOD.getMatcher(input)) != null) return controller.cheatPlantFood(matcher);
        if ((matcher = GameMenuCommand.SHOW_MAP.getMatcher(input)) != null) return controller.showMap(matcher);
        if ((matcher = GameMenuCommand.SHOW_PLANTS_STATUS.getMatcher(input)) != null) return controller.showPlantsStatus(matcher);
        if ((matcher = GameMenuCommand.SHOW_TILE_STATUS.getMatcher(input)) != null) return controller.showTileStatus(matcher);
        if ((matcher = GameMenuCommand.ZOMBIES_INFO.getMatcher(input)) != null) return controller.zombiesInfo(matcher);
        if ((matcher = GameMenuCommand.CHEAT_SPAWN_ZOMBIE.getMatcher(input)) != null) return controller.cheatSpawnZombie(matcher);
        return new Result("Invalid command in Battle.", this);
    }

    @Override
    public String getName(){
        return "Battle menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
