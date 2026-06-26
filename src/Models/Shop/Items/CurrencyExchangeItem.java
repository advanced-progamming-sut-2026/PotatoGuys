package Models.Shop.Items;

import Models.Shop.Currency;
import Models.Shop.Price;
import Models.Shop.ShopItem;
import Models.User.User;

public class CurrencyExchangeItem extends ShopItem {

    public CurrencyExchangeItem() {
        this.id = 5;
        this.name = "Currency Exchange";
        this.price = new Price(Currency.DIAMOND, 5);
        this.unitAmount = 500;
        this.maxPurchasePerUser = 0;
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
