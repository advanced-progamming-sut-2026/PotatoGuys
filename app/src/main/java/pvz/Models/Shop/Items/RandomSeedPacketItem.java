package pvz.Models.Shop.Items;

import pvz.Models.Shop.Currency;
import pvz.Models.Shop.Price;
import pvz.Models.Shop.ShopItem;
import pvz.Models.User.User;

public class RandomSeedPacketItem extends ShopItem {

    public RandomSeedPacketItem() {
        this.id = 3;
        this.name = "Random Seed Packet";
        this.price = new Price(Currency.COIN, 1000);
        this.unitAmount = 5;
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
