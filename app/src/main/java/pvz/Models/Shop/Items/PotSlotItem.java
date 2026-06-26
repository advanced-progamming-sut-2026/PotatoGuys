package pvz.Models.Shop.Items;

import pvz.Models.Shop.Currency;
import pvz.Models.Shop.Price;
import pvz.Models.Shop.ShopItem;
import pvz.Models.User.User;

public class PotSlotItem extends ShopItem {

    public PotSlotItem() {
        this.id = 1;
        this.name = "Pot";
        this.price = new Price(Currency.COIN, 2000);
        this.unitAmount = 1;
        this.maxPurchasePerUser = 20;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        if (user == null || count <= 0) {
            return false;
        }

        // return user.getGreenHouse().canUnlockMorePots(count);
        return true;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (user == null || count <= 0) {
            return false;
        }

        // return user.getGreenHouse().unlockPots(count);
        return true;
    }
}
