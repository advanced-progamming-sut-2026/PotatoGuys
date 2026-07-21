package pvz.models.leaderboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import pvz.models.Constants;
import pvz.models.user.User;
import pvz.utils.SaveManager;

public class LeaderBoard {

    public static List<LeaderBoardEntry> loadAll() {
        List<LeaderBoardEntry> entries = new ArrayList<>();
        File dir = new File(Constants.SAVE_PATH + "users");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && !name.equals("username.json"));
        if (files == null) return entries;

        for (File file : files) {
            User user = SaveManager.getInstance().loadAbsolute(file.getAbsolutePath(), User.class);
            if (user == null) continue;
            entries.add(new LeaderBoardEntry(user));
        }
        return entries;
    }

    public static List<LeaderBoardEntry> sort(List<LeaderBoardEntry> entries, LeaderBoardSortField field, SortType sortType) {
        Comparator<LeaderBoardEntry> comp = switch (field) {
            case LAST_LEVEL_AND_SEASON -> Comparator
                    .comparingInt(LeaderBoardEntry::getLastLevel)
                    .thenComparingInt(LeaderBoardEntry::getLastSeason);
            case MINI_GAMES_PASSED -> Comparator.comparingInt(LeaderBoardEntry::getNumMiniGames);
            case DAILY_QUESTS_COMPLETED -> Comparator.comparingInt(LeaderBoardEntry::getDailyQuests);
            case NON_DAILY_QUESTS_COMPLETED -> Comparator.comparingInt(LeaderBoardEntry::getNonDailyQuests);
            case HIGHEST_SCORING_GAME_SCORE -> Comparator.comparingInt(LeaderBoardEntry::getHighestScore);
        };
        if (sortType == SortType.DESCENDING) {
            comp = comp.reversed();
        }
        comp = comp.thenComparing(LeaderBoardEntry::getUsername);
        List<LeaderBoardEntry> sorted = new ArrayList<>(entries);
        sorted.sort(comp);
        return sorted;
    }

    public static String format(List<LeaderBoardEntry> entries, LeaderBoardSortField field, SortType sortType) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LEADERBOARD ===");
        sb.append("\nSorted by: ").append(field.name()).append(" (").append(sortType.name()).append(")");
        sb.append("\n---");
        if (entries.isEmpty()) {
            sb.append("\nNo players found.");
            return sb.toString();
        }
        for (int i = 0; i < entries.size(); i++) {
            LeaderBoardEntry e = entries.get(i);
            sb.append("\n").append(i + 1).append(". ");
            sb.append(e.getUsername());
            sb.append(" | Chapter: ").append(e.getLastSeasonName() != null ? e.getLastSeasonName() : "N/A")
              .append(" Lv.").append(e.getLastLevel());
            sb.append(" | MiniGames: ").append(e.getNumMiniGames());
            sb.append(" | Daily: ").append(e.getDailyQuests());
            sb.append(" | Non-Daily: ").append(e.getNonDailyQuests());
            sb.append(" | Best Score: ").append(e.getHighestScore());
        }
        return sb.toString();
    }

    public static class LeaderBoardEntry {
        private final String username;
        private final int lastLevel;
        private final int lastSeason;
        private final String lastSeasonName;
        private final int numMiniGames;
        private final int dailyQuests;
        private final int nonDailyQuests;
        private final int highestScore;

        public LeaderBoardEntry(User user) {
            this.username = user.getNickName() != null ? user.getNickName() : user.getUsername();
            this.lastLevel = user.getScore().getLastLevel();
            this.lastSeason = user.getScore().getLastSeason();
            String seasonName = null;
            if (user.getProfile() != null && user.getProfile().getSeasons() != null
                    && lastSeason >= 0 && lastSeason < user.getProfile().getSeasons().size()) {
                seasonName = user.getProfile().getSeasons().get(lastSeason).getName();
            }
            this.lastSeasonName = seasonName;
            this.numMiniGames = user.getScore().getNumMiniGames();
            this.dailyQuests = user.getScore().getDailyQuests();
            this.nonDailyQuests = user.getScore().getNonDailyQuests();
            this.highestScore = user.getScore().getHighestScore();
        }

        public String getUsername() { return username; }
        public int getLastLevel() { return lastLevel; }
        public int getLastSeason() { return lastSeason; }
        public String getLastSeasonName() { return lastSeasonName; }
        public int getNumMiniGames() { return numMiniGames; }
        public int getDailyQuests() { return dailyQuests; }
        public int getNonDailyQuests() { return nonDailyQuests; }
        public int getHighestScore() { return highestScore; }
    }
}
