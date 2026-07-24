package pvz.models.leaderboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import pvz.models.Constants;
import pvz.models.user.User;
import pvz.models.quests.Quest;
import pvz.models.quests.QuestCategory;
import pvz.utils.SaveManager;

public class Leaderboard {

    public static class LeaderBoardEntry {
        public String username;
        public int lastSeason;
        public int lastLevel;
        public int miniGamesPassed;
        public int dailyQuestsCompleted;
        public int nonDailyQuestsCompleted;
        public int highestScore;

        public LeaderBoardEntry(String username, int lastSeason, int lastLevel, int miniGamesPassed,
                int dailyQuestsCompleted, int nonDailyQuestsCompleted, int highestScore) {
            this.username = username;
            this.lastSeason = lastSeason;
            this.lastLevel = lastLevel;
            this.miniGamesPassed = miniGamesPassed;
            this.dailyQuestsCompleted = dailyQuestsCompleted;
            this.nonDailyQuestsCompleted = nonDailyQuestsCompleted;
            this.highestScore = highestScore;
        }
    }

    public static List<LeaderBoardEntry> loadAll() {
        List<LeaderBoardEntry> entries = new ArrayList<>();
        File dir = new File(Constants.SAVE_PATH + "users/");

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".json") && !f.getName().equals("username.json")) {
                        User u = SaveManager.getInstance().loadAbsolute(f.getAbsolutePath(), User.class);
                        if (u != null) {

                            // Dynamically count completed quests to prevent desync bugs
                            int daily = 0;
                            int nonDaily = 0;
                            if (u.getQuestLog() != null) {
                                for (Quest q : u.getQuestLog().getAllQuests()) {
                                    if (q.isCompleted() || q.isClaimed()) {
                                        if (q.getCategory() == QuestCategory.DAILY) {
                                            daily++;
                                        } else {
                                            nonDaily++;
                                        }
                                    }
                                }
                            }

                            int season = u.getScore() != null ? u.getScore().getLastSeason() : 0;
                            int level = u.getScore() != null ? u.getScore().getLastLevel() : 0;
                            int miniGames = u.getScore() != null ? u.getScore().getMiniGamesPassed() : 0;
                            int highScore = u.getScore() != null ? u.getScore().getHighestScore() : 0;

                            entries.add(new LeaderBoardEntry(
                                    u.getUsername(), season, level, miniGames, daily, nonDaily, highScore));
                        }
                    }
                }
            }
        }
        return entries;
    }

    public static List<LeaderBoardEntry> sort(List<LeaderBoardEntry> list, LeaderboardSortField field,
            SortTypes order) {
        Comparator<LeaderBoardEntry> comp = switch (field) {
            case LAST_LEVEL_AND_SEASON -> Comparator.comparingInt((LeaderBoardEntry e) -> e.lastSeason)
                    .thenComparingInt(e -> e.lastLevel);
            case MINI_GAMES_PASSED -> Comparator.comparingInt(e -> e.miniGamesPassed);
            case DAILY_QUESTS_COMPLETED -> Comparator.comparingInt(e -> e.dailyQuestsCompleted);
            case NON_DAILY_QUESTS_COMPLETED -> Comparator.comparingInt(e -> e.nonDailyQuestsCompleted);
            case HIGHEST_SCORING_GAME_SCORE -> Comparator.comparingInt(e -> e.highestScore);
        };

        if (order == SortTypes.DESCENDING) {
            comp = comp.reversed();
        }

        // Tie-breaker: Alphabetical username sorting
        comp = comp.thenComparing(e -> e.username);
        list.sort(comp);
        return list;
    }

    public static String format(List<LeaderBoardEntry> list, LeaderboardSortField field, SortTypes order) {
        if (list.isEmpty())
            return "No players found on the leaderboard.";

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== LEADERBOARD (Sorted by ").append(field).append(" ").append(order).append(") ===\n");
        sb.append(String.format("%-15s | %-22s | %-10s | %-12s | %-16s | %-13s\n",
                "Username", "Story Mode", "Minigames", "Daily Quests", "Non-Daily Quests", "Highest Score"));
        sb.append("-".repeat(102)).append("\n");

        for (LeaderBoardEntry e : list) {
            String levelStr;
            if (e.lastLevel > 0 && e.lastSeason > 0) {
                levelStr = "Level " + e.lastLevel + " of Chapter " + e.lastSeason;
            } else if (e.lastLevel > 0) {
                levelStr = "Level " + e.lastLevel;
            } else {
                levelStr = "None";
            }

            sb.append(String.format("%-15s | %-22s | %-10d | %-12d | %-16d | %-13d\n",
                    e.username, levelStr, e.miniGamesPassed, e.dailyQuestsCompleted, e.nonDailyQuestsCompleted,
                    e.highestScore));
        }
        return sb.toString();
    }

}