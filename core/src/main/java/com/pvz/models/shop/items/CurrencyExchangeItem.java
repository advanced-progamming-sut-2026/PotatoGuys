package com.pvz.models.shop.items;

import com.pvz.models.shop.Currency;
import com.pvz.models.shop.Price;
import com.pvz.models.shop.ShopItem;
import com.pvz.models.user.User;

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
        if (user == null || count <= 0) return false;

        int totalCost = price.getAmount() * count;
        if (user.getProfile().getDiamonds() < totalCost) return false;

        return true;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        int totalCost = price.getAmount() * count;
        int coinsGained = unitAmount * count;

        user.getProfile().setDiamonds(user.getProfile().getDiamonds() - totalCost);
        user.getProfile().setCoins(user.getProfile().getCoins() + coinsGained);

        user.saveUser();
        return true;
    }
}
