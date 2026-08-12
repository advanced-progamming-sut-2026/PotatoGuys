package com.pvz.models.shop;

import com.pvz.models.user.User;

public abstract class ShopItem {

    protected int id;
    protected String name;
    protected String description;
    protected Price price;
    protected int unitAmount;
    protected int maxPurchasePerUser;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public abstract boolean canBuy(User user, int count, String plantType);

    public abstract boolean applyEffect(User user, int count, String plantType);
}
