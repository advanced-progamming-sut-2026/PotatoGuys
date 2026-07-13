package pvz.Models.User;

import java.util.ArrayList;

import pvz.Models.Seasons.SeasonProgress;

public class Profile {
    private int gamePlayed;
    private int coins;
    private int diamonds;
    private Collection collection;
    private ArrayList<SeasonProgress> seasonProgresses;
    private int maxMiopoint;

    public Profile(){
        this.collection=new Collection();
    }

    public int getGamePlayed() {
        return gamePlayed;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
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
