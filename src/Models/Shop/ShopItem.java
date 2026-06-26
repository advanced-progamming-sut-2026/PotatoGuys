package Models.Shop;

public abstract class ShopItem {

    protected int id;
    protected String name;
    protected Price price;
    protected int unitAmount;
    protected int maxPurchasePerUser;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Price getPrice() {
        return price;
    }

    public int getUnitAmount() {
        return unitAmount;
    }

    public int getMaxPurchasePerUser() {
        return maxPurchasePerUser;
    }

    public abstract boolean canBuy(Models.User.User user, int count, String plantType);

    public abstract boolean applyEffect(Models.User.User user, int count, String plantType);
}
