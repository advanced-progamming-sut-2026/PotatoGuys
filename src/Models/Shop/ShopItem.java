package Models.Shop;

import Models.Plants.Enums.PlantType;
import Models.User.User;

public abstract class ShopItem {

    protected int id;
    protected String name;
    protected Price price;
    protected int maxPurchasePerUser;
    protected int priceCoin;
    protected int priceDiamond;

    public abstract void applyEffect(User user, int count, String plantType);
}
