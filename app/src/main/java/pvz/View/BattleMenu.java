package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.BattleController;
import pvz.Models.Commands.BattleMenuCommand;

public class BattleMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        BattleController controller=new BattleController();
        Matcher matcher;
        if ((matcher = BattleMenuCommand.ADVANCE_TIME.getMatcher(input)) != null) return controller.advanceTime(matcher);
        if ((matcher = BattleMenuCommand.COLLECT_SUN.getMatcher(input)) != null) return controller.collectSun(matcher);
        if ((matcher = BattleMenuCommand.SHOW_SUN.getMatcher(input)) != null) return controller.showSun(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_SUN.getMatcher(input)) != null) return controller.cheatSun(matcher);
        if ((matcher = BattleMenuCommand.RELEASE_NUKE.getMatcher(input)) != null) return controller.releaseNuke(matcher);
        if ((matcher = BattleMenuCommand.PLANT.getMatcher(input)) != null) return controller.plant(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_COOLDOWN.getMatcher(input)) != null) return controller.cheatCooldown(matcher);
        if ((matcher = BattleMenuCommand.PLUCK_PLANT.getMatcher(input)) != null) return controller.pluckPlant(matcher);
        if ((matcher = BattleMenuCommand.FEED_PLANT.getMatcher(input)) != null) return controller.feedPlant(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_PLANT_FOOD.getMatcher(input)) != null) return controller.cheatPlantFood(matcher);
        if ((matcher = BattleMenuCommand.SHOW_MAP.getMatcher(input)) != null) return controller.showMap(matcher);
        if ((matcher = BattleMenuCommand.SHOW_PLANTS_STATUS.getMatcher(input)) != null) return controller.showPlantsStatus(matcher);
        if ((matcher = BattleMenuCommand.SHOW_TILE_STATUS.getMatcher(input)) != null) return controller.showTileStatus(matcher);
        if ((matcher = BattleMenuCommand.ZOMBIES_INFO.getMatcher(input)) != null) return controller.zombiesInfo(matcher);
        if ((matcher = BattleMenuCommand.CHEAT_SPAWN_ZOMBIE.getMatcher(input)) != null) return controller.cheatSpawnZombie(matcher);
        return new Result("Invalid command in Battle.", this);
    }

    @Override
    public String getName(){
        return "Battle menu";
    }
}
