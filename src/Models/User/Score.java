package Models.User;

public class Score {
    private int lastLevel;
    private int lastSeason;
    private int numMiniGames;
    private int dailyQuests;
    private int nonDailyQuests;
    private int highestScore;
    public int getLastLevel() {
        return lastLevel;
    }
    public int getLastSeason() {
        return lastSeason;
    }
    public int getNumMiniGames() {
        return numMiniGames;
    }
    public int getDailyQuests() {
        return dailyQuests;
    }
    public int getNonDailyQuests() {
        return nonDailyQuests;
    }
    public int getHighestScore() {
        return highestScore;
    }
}
