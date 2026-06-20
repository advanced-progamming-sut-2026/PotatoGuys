package Models.Shop.Items;

import Models.Shop.Currency;
import Models.Shop.Price;
import Models.Shop.ShopItem;
import Models.User.User;

public class PotSlotItem extends ShopItem {
    public PotSlotItem(){

    }

    @Override
    public void applyEffect(User user, int count, String plantType){
        this.name = "Gold Pot";
        this.price = new Price(Currency.COIN, 2000);
        this.maxPurchasePerUser = 20;
    }
}
