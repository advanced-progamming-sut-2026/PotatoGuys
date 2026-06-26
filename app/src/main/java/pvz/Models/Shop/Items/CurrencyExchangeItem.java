package pvz.Models.Shop.Items;

import pvz.Models.Shop.Currency;
import pvz.Models.Shop.Price;
import pvz.Models.Shop.ShopItem;
import pvz.Models.User.User;

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
