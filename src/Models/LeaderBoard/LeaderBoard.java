import Models.User.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderBoard {

    public ArrayList<User> sortUsers(List<User> users , LeaderBoardSortField sortField , SortType sortType) {
        ArrayList<User> sortedUsers = new ArrayList<>(users);
        Comparator<User> comparator = getComparator (sortField);

        if (sortType == SortType.DESCENDING){
            comparator = comparator.reversed();
        }

        sortedUsers.sort(comparator.thenComparing(User::getUsername));
        return sortedUsers;
    }

    private Comparator<Users> getComparator(LeaderBoardSortField sortField){
        return switch (sortField) {
            case LAST_LEVEL_AND_SEASON -> Comparator
                    .comparingInt(User::getLastUnlockedLevel)
                    .thenComparingInt(User::getLastUnlockedSeason);
            case MINI_GAMES_PASSED -> Comparator
                    .comparingInt(User::getPassedMiniGamesCount)
            case DAILY_QUESTS_COMPLETED -> Comparator
                    .comparingInt(User::getCompletedDailyQuests);
            case NON_DAILY_QUESTS_COMPLETED -> Comparator
                .comparingInt(User::getCompletedNonDailyQuests);
            case HIGHEST_SCORING_GAME_SCORE -> Comparator
                    .comparingInt(User::getHighestScoringGameScore);
        }
    }

}
