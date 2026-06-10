package Models.User;

import java.util.ArrayList;

import Models.Seasons.SeasonProgress;

public class Profile {
    private int gamePlayed;
    private int coins;
    private int diamonds;
    private Collection collection;
    private ArrayList<SeasonProgress> seasonProgresses;
    private int maxMiopoint;

    public int getGamePlayed() {
        return gamePlayed;
    }

    public int getCoins() {
        return coins;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public Collection getCollection() {
        return collection;
    }

    public ArrayList<SeasonProgress> getSeasonProgresses() {
        return seasonProgresses;
    }

    public int getMaxMiopoint() {
        return maxMiopoint;
    }
}
