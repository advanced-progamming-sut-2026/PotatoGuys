package pvz.models.user;

import java.util.ArrayList;

import pvz.models.entities.plants.enums.PlantType;
import pvz.models.games.seasons.Season;

public class Profile {
    private int gamePlayed;
    private int coins;
    private int diamonds;
    private int plantFood;
    private Collection collection;
    private News news;
    private GreenHouseCollection greenHouseCollection;
    private ArrayList<Season> seasons;
    private int maxMiopoint;
    private PlantType dailyOfferPlantType;
    private String dailyOfferDate;
    private boolean dailyOfferPurchased;

    public Profile(){
        this.news = new News();
        this.collection = new Collection(news);
        this.greenHouseCollection = new GreenHouseCollection();
        seasons = new ArrayList<>();
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

    public int getPlantFood() {
        return plantFood;
    }

    public void setPlantFood(int plantFood) {
        this.plantFood = plantFood;
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

    public PlantType getDailyOfferPlantType() {
        return dailyOfferPlantType;
    }

    public void setDailyOfferPlantType(PlantType dailyOfferPlantType) {
        this.dailyOfferPlantType = dailyOfferPlantType;
    }

    public String getDailyOfferDate() {
        return dailyOfferDate;
    }

    public void setDailyOfferDate(String dailyOfferDate) {
        this.dailyOfferDate = dailyOfferDate;
    }

    public boolean isDailyOfferPurchased() {
        return dailyOfferPurchased;
    }

    public void setDailyOfferPurchased(boolean dailyOfferPurchased) {
        this.dailyOfferPurchased = dailyOfferPurchased;
    }

    public Season getSeasonByName(String name){
        for (Season s : seasons){
            if (s.getName().equalsIgnoreCase(name)){
                return s;
            }
        }
        return null;
    }

    public void addCoins(int amount){
        coins += amount;
    }

    public void addDiamonds(int amount){
        diamonds += amount;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }
}
