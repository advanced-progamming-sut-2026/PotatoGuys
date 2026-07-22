package pvz.controller;

import java.util.List;
import java.util.regex.Matcher;

import pvz.models.leaderboard.Leaderboard;
import pvz.models.leaderboard.LeaderboardSortField;
import pvz.models.leaderboard.SortTypes;
import pvz.models.leaderboard.Leaderboard.LeaderBoardEntry;
import pvz.view.MainMenu;
import pvz.view.Result;

public class LeaderBoardController {
    private LeaderboardSortField currentField = LeaderboardSortField.HIGHEST_SCORING_GAME_SCORE;
    private SortTypes currentSortTypes = SortTypes.DESCENDING;

    public Result show(Matcher matcher) {
        List<LeaderBoardEntry> all = Leaderboard.loadAll();
        String output = Leaderboard.format(all, currentField, currentSortTypes);
        return new Result(output);
    }

    public Result sort(Matcher matcher) {
        String fieldStr = matcher.group(1);
        String orderStr = matcher.group(2);

        try {
            currentField = LeaderboardSortField.valueOf(fieldStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Result("Invalid sort field. Valid fields: LAST_LEVEL_AND_SEASON, MINI_GAMES_PASSED, DAILY_QUESTS_COMPLETED, NON_DAILY_QUESTS_COMPLETED, HIGHEST_SCORING_GAME_SCORE");
        }

        if (orderStr != null) {
            try {
                currentSortTypes = SortTypes.valueOf(orderStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return new Result("Invalid order. Use ASCENDING or DESCENDING.");
            }
        }

        List<LeaderBoardEntry> all = Leaderboard.loadAll();
        String output = Leaderboard.format(all, currentField, currentSortTypes);
        return new Result(output);
    }

    public Result exit(Matcher matcher) {
        return new Result("Exited to Main menu.", new MainMenu());
    }
}
