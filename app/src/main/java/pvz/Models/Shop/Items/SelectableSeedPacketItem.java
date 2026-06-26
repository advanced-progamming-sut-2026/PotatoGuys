package pvz.Models.Shop.Items;

import pvz.Models.Shop.Currency;
import pvz.Models.Shop.Price;
import pvz.Models.Shop.ShopItem;
import pvz.Models.User.User;

public class SelectableSeedPacketItem extends ShopItem {

    public SelectableSeedPacketItem() {
        this.id = 4;
        this.name = "Selectable Seed Packet";
        this.price = new Price(Currency.DIAMOND, 5);
        this.unitAmount = 10;
        this.maxPurchasePerUser = 0;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        return user != null && count > 0 && plantType != null && !plantType.isBlank();
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        return user != null && count > 0 && plantType != null && !plantType.isBlank();
    }
}
