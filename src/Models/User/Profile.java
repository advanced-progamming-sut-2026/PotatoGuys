package Models.User;

import java.util.ArrayList;

import Models.Plants.Enums.PlantStorage;
import Models.Seasons.SeasonProgress;

public class Profile {
    private int gamePlayed;
    private int coins;
    private int diamonds;
    private ArrayList<PlantStorage> myPlants;
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
    public ArrayList<PlantStorage> getMyPlants() {
        return myPlants;
    }
    public ArrayList<SeasonProgress> getSeasonProgresses() {
        return seasonProgresses;
    }
    public int getMaxMiopoint() {
        return maxMiopoint;
    }

}
