package pvz.View;

import pvz.Models.toDel.Seasons.AncientEgypt;

public class ChapterMenu implements Menu{
    private String seasonName;

    public ChapterMenu(String seasonName){
        this.seasonName = seasonName;
    }

    @Override
    public Result handleInput(String input) {
        if (input.equals("1")){
            return new Result(new PreGameMenu(new AncientEgypt(), 1));
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
