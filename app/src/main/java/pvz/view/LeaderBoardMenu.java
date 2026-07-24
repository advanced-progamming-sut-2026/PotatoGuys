package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.LeaderBoardController;
import pvz.enums.commands.LeaderBoardCommands;

public class LeaderBoardMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        LeaderBoardController controller = new LeaderBoardController();
        Matcher matcher;
        if ((matcher = LeaderBoardCommands.SHOW.getMatcher(input)) != null)
            return controller.show(matcher);
        if ((matcher = LeaderBoardCommands.SORT.getMatcher(input)) != null)
            return controller.sort(matcher);
        if ((matcher = LeaderBoardCommands.EXIT.getMatcher(input)) != null)
            return controller.exit(matcher);
        if ((matcher = LeaderBoardCommands.HELP.getMatcher(input)) != null)
            return new Result(LeaderBoardCommands.getHelp());
        return new Result("Invalid command. Use: leaderboard, leaderboard sort -s <field> -o <asc|desc>, menu exit",
                this);
    }

    @Override
    public String getName() {
        return "Leaderboard";
    }

    @Override
    public Result onEnter() {
        return new Result(
                "Entered Leaderboard. Use 'leaderboard' to view, 'leaderboard sort -s <field> -o <asc|desc>' to sort.");
    }
}
