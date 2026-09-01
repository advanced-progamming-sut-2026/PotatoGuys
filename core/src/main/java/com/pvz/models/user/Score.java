package com.pvz.models.user;
import com.google.gson.annotations.SerializedName;

public class Score {
    private int lastSeason = 0;
    private int lastLevel = 0;

    @SerializedName("numMiniGames")
    private int miniGamesPassed = 0;

    private int dailyQuests = 0;
    private int nonDailyQuests = 0;
    private int highestScore = 0;
    private int bestMiopoint = 0;

    public int getLastSeason() { return lastSeason; }
    public void setLastSeason(int lastSeason) { this.lastSeason = lastSeason; }

    public int getLastLevel() { return lastLevel; }
    public void setLastLevel(int lastLevel) { this.lastLevel = lastLevel; }

    public int getMiniGamesPassed() { return miniGamesPassed; }
    public void setMiniGamesPassed(int miniGamesPassed) { this.miniGamesPassed = miniGamesPassed; }

    public int getDailyQuests() { return dailyQuests; }
    public void setDailyQuests(int dailyQuests) { this.dailyQuests = dailyQuests; }

    public int getNonDailyQuests() { return nonDailyQuests; }
    public void setNonDailyQuests(int nonDailyQuests) { this.nonDailyQuests = nonDailyQuests; }

    public int getHighestScore() { return highestScore; }
    public void setHighestScore(int highestScore) { this.highestScore = highestScore; }

    public int getBestMiopoint() { return bestMiopoint; }
    public void setBestMiopoint(int bestMiopoint) { this.bestMiopoint = bestMiopoint; }
}
