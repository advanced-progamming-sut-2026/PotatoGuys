package pvz.View.Game.modals;

import java.util.regex.Matcher;

import pvz.Controller.Game.GameController;
import pvz.Enums.Commands.GameMenuCommands;
import pvz.Models.AppContext;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelLoader;
import pvz.Models.Games.Seasons.Season;
import pvz.View.Menu;
import pvz.View.Result;
import pvz.View.Game.PreGameMenu;
import pvz.View.Game.RunningGameMenu;

public class SelectLevelModal implements Menu{
    private Season season;

    public SelectLevelModal(String seasonName){
        season = AppContext.getInstance().getCurrentUser().getProfile().getSeasonByName(seasonName);
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommands.SELECT_LEVEL.getMatcher(input)) != null) {
            int levelNumber = Integer.parseInt(matcher.group("level"));
            if(!season.isLevelUnlocked(levelNumber)){
                return new Result("This level is locked! select another level.");
            }
            Level level = LevelLoader.loadLevel(season.getName(), levelNumber);
            if(level.hasPreGame())
                return new Result(new PreGameMenu(level));
            GameContext context = new GameContext(level);
            AppContext.getInstance().setGameContext(context);
            return new Result("Game started!" , new RunningGameMenu(new GameController(context)));
        }
        return new Result("Invalid command in Select Level Menu");
    }

    @Override
    public String getName() {
        return "Select Level Menu";
    }

    @Override
    public Result onEnter() {
        return new Result("Enter level number to play.");
    }
}
