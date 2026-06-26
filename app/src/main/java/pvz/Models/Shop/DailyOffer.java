package pvz.Models.Shop;

import pvz.Models.Plants.Enums.PlantType;
import pvz.Models.User.User;

import java.time.LocalDate;

public class DailyOffer extends ShopItem {

    private PlantType plantType;
    private LocalDate offerDate;
    private boolean purchasedToday;

    public DailyOffer(PlantType plantType, LocalDate offerDate) {
        this.id = id;
        this.name = "Daily Offer";
        this.price = new Price(Currency.COIN, 1600);
        this.unitAmount = 10;
        this.maxPurchasePerUser = 1;
        this.plantType = plantType;
        this.offerDate = offerDate;
        this.purchasedToday = false;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public LocalDate getOfferDate() {
        return offerDate;
    }

    public boolean isPurchasedToday() {
        return purchasedToday;
    }

    public boolean isExpired(LocalDate today) {
        return !offerDate.equals(today);
    }

    public void refresh(PlantType plantType) {
        this.plantType = plantType;
        this.offerDate = LocalDate.now();
        this.purchasedToday = false;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        return count == 1 && !purchasedToday;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) {
            return false;
        }

        purchasedToday = true;
        return true;
    }
}
