package Models.Shop.Items;

import Models.Shop.Currency;
import Models.Shop.Price;
import Models.Shop.ShopItem;
import Models.User.User;

public class PlantFoodItem extends ShopItem {

    public PlantFoodItem() {
        this.id = 2;
        this.name = "Plant Food";
        this.price = new Price(Currency.DIAMOND, 3);
        this.unitAmount = 1;
        this.maxPurchasePerUser = 3;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        return user != null && count > 0;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        return user != null && count > 0;
    }
}
