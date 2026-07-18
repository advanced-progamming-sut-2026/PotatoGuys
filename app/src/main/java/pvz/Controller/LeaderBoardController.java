package pvz.Controller;

import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.LeaderBoard.LeaderBoard;
import pvz.Models.LeaderBoard.LeaderBoard.LeaderBoardEntry;
import pvz.Models.LeaderBoard.LeaderBoardSortField;
import pvz.Models.LeaderBoard.SortType;
import pvz.View.MainMenu;
import pvz.View.Result;

public class LeaderBoardController {
    private LeaderBoardSortField currentField = LeaderBoardSortField.HIGHEST_SCORING_GAME_SCORE;
    private SortType currentSortType = SortType.DESCENDING;

    public Result show(Matcher matcher) {
        List<LeaderBoardEntry> all = LeaderBoard.loadAll();
        String output = LeaderBoard.format(all, currentField, currentSortType);
        return new Result(output);
    }

    public Result sort(Matcher matcher) {
        String fieldStr = matcher.group(1);
        String orderStr = matcher.group(2);

        try {
            currentField = LeaderBoardSortField.valueOf(fieldStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Result("Invalid sort field. Valid fields: LAST_LEVEL_AND_SEASON, MINI_GAMES_PASSED, DAILY_QUESTS_COMPLETED, NON_DAILY_QUESTS_COMPLETED, HIGHEST_SCORING_GAME_SCORE");
        }

        if (orderStr != null) {
            try {
                currentSortType = SortType.valueOf(orderStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return new Result("Invalid order. Use ASCENDING or DESCENDING.");
            }
        }

        List<LeaderBoardEntry> all = LeaderBoard.loadAll();
        String output = LeaderBoard.format(all, currentField, currentSortType);
        return new Result(output);
    }

    public Result exit(Matcher matcher) {
        return new Result("Exited to Main menu.", new MainMenu());
    }
}
