package pvz.View;

public class GameModesMenu implements Menu{
    @Override
    public Result handleInput(String input) {
        if (input.equals("1")){
            return new Result("",new SeasonMenu());
        } else if(input.equals("2")){
            return new Result("",new MiniGamesMenu());
        } else {
            return new Result("Invalid Command");
        }
    }

    @Override
    public String getName() {
        return "Game Modes";
    }

    @Override
    public Result onEnter() {
        return new Result("Select Game Mode:\n1.Adventure\n2.Mini Games");
    }
}
