package pvz.View.Game;

import pvz.Controller.Game.NormalGameController;
import pvz.Enums.Commands.GameMenuCommand;
import pvz.Models.Seasons.Levels.NormalLevel;
import pvz.View.Result;

public class NormalGameMenu extends GameMenu{
        NormalGameController controller;
        public NormalGameMenu(NormalLevel level){
                super(new NormalGameController(level));
        }
        @Override
        public Result handleInput(String input) {
                Result result= super.handleInput(input);
                if (result!=null) return result;
                if ((matcher = GameMenuCommand.COLLECT_SUN.getMatcher(input)) != null) return controller.collectSun(matcher);
                if ((matcher = GameMenuCommand.SHOW_SUN.getMatcher(input)) != null) return controller.showSun(matcher);
                if ((matcher = GameMenuCommand.CHEAT_SUN.getMatcher(input)) != null) return controller.cheatSun(matcher);
                if ((matcher = GameMenuCommand.PLANT.getMatcher(input)) != null) return controller.plant(matcher);
                if ((matcher = GameMenuCommand.PLUCK_PLANT.getMatcher(input)) != null) return controller.pluckPlant(matcher);
                return new Result("Invalid command in Game.", this);
        }
}
