package Models.Shop;

import Models.Plants.Enums.PlantType;
import Models.User.User;

import java.time.LocalDate;

public class DailyOffer extends ShopItem {

    private PlantType plantType;
    private LocalDate offerDate;
    private boolean purchasedToday;

    public DailyOffer(PlantType plantType, LocalDate date){

    }

    public boolean isExpired(LocalDate today) {
        return false;
    }

    public void refresh(){

    }

    @Override
    public void applyEffect(User user, int count, String plantType) {

    }
}
