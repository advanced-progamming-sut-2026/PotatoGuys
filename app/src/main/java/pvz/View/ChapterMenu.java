package pvz.View;


import pvz.Models.AppContext;
import pvz.Models.Games.Seasons.Season;

import java.util.List;

public class ChapterMenu implements Menu{
    private String seasonName;

    public ChapterMenu(String seasonName){
        this.seasonName = seasonName;
    }

    @Override
    public Result handleInput(String input) {
        List<Season> seasons=AppContext.getInstance().getCurrentUser().getProfile().getSeasons();
        if (input.equals("1")){
            return new Result(new PreGameMenu(seasons.getFirst(), 1));
        }
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
