package Models.Shop;

import Models.User.User;

public abstract class ShopItem {

    protected int id;
    protected String name;
    protected Price price;
    protected int maxPurchasePerUser;

    public abstract void applyEffect(User user, int count, String plantType);
}
