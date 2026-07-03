package pvz.View;

public class ChapterMenu implements Menu{

    public ChapterMenu(String seasonName){

    }
    @Override
    public Result handleInput(String input) {
        if (input.equals("1")){
            return new Result(new PreGameMenu());
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
