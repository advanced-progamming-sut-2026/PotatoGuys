package pvz.models.user;

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

    public void setLastLevel(int lastLevel) {
        this.lastLevel = lastLevel;
    }

    public int getLastSeason() {
        return lastSeason;
    }

    public void setLastSeason(int lastSeason) {
        this.lastSeason = lastSeason;
    }

    public int getNumMiniGames() {
        return numMiniGames;
    }

    public void setNumMiniGames(int numMiniGames) {
        this.numMiniGames = numMiniGames;
    }

    public int getDailyQuests() {
        return dailyQuests;
    }

    public void setDailyQuests(int dailyQuests) {
        this.dailyQuests = dailyQuests;
    }

    public int getNonDailyQuests() {
        return nonDailyQuests;
    }

    public void setNonDailyQuests(int nonDailyQuests) {
        this.nonDailyQuests = nonDailyQuests;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }
}
