package pvz.Models.User;

import pvz.Models.Games.Seasons.Season;

import java.util.ArrayList;

public class Profile {
    private int gamePlayed;
    private int coins;
    private int diamonds;
    private Collection collection;
    private GreenHouseCollection greenHouseCollection;
    private ArrayList<Season> seasons;
    private int maxMiopoint;

    public Profile(){
        this.collection=new Collection();
        seasons=new ArrayList<>();
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

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

    public int getMaxMiopoint() {
        return maxMiopoint;
    }

    public GreenHouseCollection getGreenHouseCollection(){
        return greenHouseCollection;
    }
}
