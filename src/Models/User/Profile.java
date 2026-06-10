package Models.User;

import java.util.ArrayList;

import Models.Enums.PlantType;

public class Profile {
    private int gamePlayed;
    private int coins;
    private int diamonds;
    private ArrayList<PlantType> myPlants;
    private ArrayList<SeasonProgress> seasonProgresses;
    private int maxMiopoint;

    public Profile(int gamePlayed, int coins, int diamonds, ArrayList<Plant> myPlants, ArrayList<Chapter> mycChapters,
            int maxMiopoint) {
        this.gamePlayed = gamePlayed;
        this.coins = coins;
        this.diamonds = diamonds;
        this.myPlants = myPlants;
        this.mycChapters = mycChapters;
        this.maxMiopoint = maxMiopoint;
    }
    
    public int getGamePlayed() {
        return gamePlayed;
    }
    public void setGamePlayed(int gamePlayed) {
        this.gamePlayed = gamePlayed;
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
    public ArrayList<Plant> getMyPlants() {
        return myPlants;
    }
    public void setMyPlants(ArrayList<Plant> myPlants) {
        this.myPlants = myPlants;
    }
    public ArrayList<Chapter> getMycChapters() {
        return mycChapters;
    }
    public void setMycChapters(ArrayList<Chapter> mycChapters) {
        this.mycChapters = mycChapters;
    }
    public int getMaxMiopoint() {
        return maxMiopoint;
    }
    public void setMaxMiopoint(int maxMiopoint) {
        this.maxMiopoint = maxMiopoint;
    }
        public ArrayList<SeasonProgress> getSeasonProgresses() {
        return seasonProgresses;
    }
}
