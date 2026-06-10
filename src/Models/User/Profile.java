package Models.User;

import java.util.ArrayList;

import Models.Enums.PlantType;
import Models.Plants.Plant;

public class Profile {
    private int gamePlayed;
    private int coins;
    private int diamonds;

    private ArrayList<PlantType> myPlants;
    private Map<> mycChapters;
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
    public int getCoins() {
        return coins;
    }
    public int getDiamonds() {
        return diamonds;
    }
    public ArrayList<Plant> getMyPlants() {
        return myPlants;
    }
    public ArrayList<Chapter> getMycChapters() {
        return mycChapters;
    }
    public int getMaxMiopoint() {
        return maxMiopoint;
    }
}
