package View;

import Controller.BattleController;
import Models.Commands.BattleMenuCommand;

import java.util.regex.Matcher;

public class BattleMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = BattleMenuCommand.ADVANCE_TIME.getMatcher(input)) != null) return BattleController.advanceTime(matcher);
        if ((matcher = BattleMenuCommand.COLLECT_SUN.getMatcher(input)) != null) return BattleController.collectSun(matcher);
        if ((matcher = BattleMenuCommand.SHOW_SUN.getMatcher(input)) != null) return BattleController.showSun(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_SUN.getMatcher(input)) != null) return BattleController.cheatSun(matcher);
        if ((matcher = BattleMenuCommand.RELEASE_NUKE.getMatcher(input)) != null) return BattleController.releaseNuke(matcher);
        if ((matcher = BattleMenuCommand.PLANT.getMatcher(input)) != null) return BattleController.plant(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_COOLDOWN.getMatcher(input)) != null) return BattleController.cheatCooldown(matcher);
        if ((matcher = BattleMenuCommand.PLUCK_PLANT.getMatcher(input)) != null) return BattleController.pluckPlant(matcher);
        if ((matcher = BattleMenuCommand.FEED_PLANT.getMatcher(input)) != null) return BattleController.feedPlant(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_PLANT_FOOD.getMatcher(input)) != null) return BattleController.cheatPlantFood(matcher);
        if ((matcher = BattleMenuCommand.SHOW_MAP.getMatcher(input)) != null) return BattleController.showMap(matcher);
        if ((matcher = BattleMenuCommand.SHOW_PLANTS_STATUS.getMatcher(input)) != null) return BattleController.showPlantsStatus(matcher);
        if ((matcher = BattleMenuCommand.SHOW_TILE_STATUS.getMatcher(input)) != null) return BattleController.showTileStatus(matcher);
        if ((matcher = BattleMenuCommand.ZOMBIES_INFO.getMatcher(input)) != null) return BattleController.zombiesInfo(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_SPAWN_ZOMBIE.getMatcher(input)) != null) return BattleController.cheatSpawnZombie(matcher);
        return new Result("Invalid command in Battle.", this);
    }
}
