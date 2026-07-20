package pvz.View;


import java.util.List;
import java.util.regex.Matcher;

import pvz.Enums.Commands.ChapterMenuCommand;
import pvz.Models.AppContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelFactory;
import pvz.Models.Games.Levels.LevelLoader;
import pvz.Models.Games.Levels.Data.LevelDefinition;
import pvz.Models.Games.Seasons.Season;
import pvz.View.Game.PreGameMenu;

public class ChapterMenu implements Menu{
    private Season season;

    public ChapterMenu(String seasonName){
        List<Season> seasons = AppContext.getInstance().getCurrentUser().getProfile().getSeasons();
        for (Season s : seasons){
            if (s.getName().equalsIgnoreCase(seasonName)){
                season = s;
            }
        }
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = ChapterMenuCommand.SELECT_LEVEL.getMatcher(input)) != null) {
            int levelNumber = Integer.parseInt(matcher.group("level"));
            LevelDefinition levelDef = LevelLoader.loadLevel(season.getName(), levelNumber);
            Level level = LevelFactory.createLevel(levelDef);
            return new Result(new PreGameMenu(level));
        }
        if ((matcher = ChapterMenuCommand.HELP.getMatcher(input)) != null)
            return new Result(ChapterMenuCommand.getHelp());
        return new Result("Invalid command in Chapter Menu");
    }

    @Override
    public String getName() {
        return "Chapter Menu";
    }

    @Override
    public Result onEnter() {
        return new Result("Enter level number to play.");
    }
}
