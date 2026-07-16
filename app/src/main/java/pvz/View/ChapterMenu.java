package pvz.View;


import pvz.Models.AppContext;
import pvz.Models.Games.Seasons.Season;
import pvz.View.Game.PreGameMenu;

import java.util.List;

public class ChapterMenu implements Menu{
    private Season season;

    public ChapterMenu(String seasonName){
        List<Season> seasons=AppContext.getInstance().getCurrentUser().getProfile().getSeasons();
        for (Season s: seasons){
            if (s.getName().equalsIgnoreCase(seasonName)){
                season=s;
            }
        }
    }

    @Override
    public Result handleInput(String input) {
        try {
            int levelNumber=Integer.parseInt(input);
            if (levelNumber>3 || levelNumber<1){
                return new Result("Enter a number between 1 and 3");
            }
            return new Result(new PreGameMenu(season, levelNumber));
        } catch (Exception ex) {
            return new Result("Invalid command in Chapter Menu");
        }
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
