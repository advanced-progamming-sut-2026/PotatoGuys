package pvz.Models.Shop;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.User.User;

import java.time.LocalDate;

public class DailyOffer extends ShopItem {

    private PlantType plantType;
    private LocalDate offerDate;
    private boolean purchasedToday;

    public DailyOffer(PlantType plantType, LocalDate offerDate) {
        this.id = 6;
        this.name = "Daily Offer - " + plantType.name();
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

    public void setPurchasedToday(boolean purchasedToday) {
        this.purchasedToday = purchasedToday;
    }

    public boolean isExpired(LocalDate today) {
        return !offerDate.equals(today);
    }

    public void refresh(PlantType plantType) {
        this.plantType = plantType;
        this.name = "Daily Offer - " + plantType.name();
        this.offerDate = LocalDate.now();
        this.purchasedToday = false;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        if (user == null) return false;
        if (purchasedToday) return false;
        if (count != 1) return false;

        int totalCost = price.getAmount();
        if (user.getProfile().getCoins() < totalCost) return false;

        return true;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        user.getProfile().setCoins(user.getProfile().getCoins() - price.getAmount());
        purchasedToday = true;

        user.getProfile().setDailyOfferPurchased(true);
        user.saveUser();
        return true;
    }
}
