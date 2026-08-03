package com.pvz.models.shop.items;

import com.pvz.models.AppContext;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePot;
import com.pvz.models.shop.Currency;
import com.pvz.models.shop.Price;
import com.pvz.models.shop.ShopItem;
import com.pvz.models.user.User;

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
        if (user == null || count <= 0) return false;

        int totalCost = price.getAmount() * count;
        if (user.getProfile().getCoins() < totalCost) return false;

        GreenHouse gh = AppContext.getInstance().getGreenHouse();
        if (gh == null) return false;

        int lockedCount = 0;
        for (GreenHousePot pot : gh.getGreenHousePots()) {
            if (pot.isLocked()) lockedCount++;
        }
        if (count > lockedCount) return false;

        return true;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        int totalCost = price.getAmount() * count;
        user.getProfile().setCoins(user.getProfile().getCoins() - totalCost);

        GreenHouse gh = AppContext.getInstance().getGreenHouse();
        int unlocked = 0;
        for (GreenHousePot pot : gh.getGreenHousePots()) {
            if (pot.isLocked() && unlocked < count) {
                gh.unlockPot(pot.getX(), pot.getY());
                unlocked++;
            }
        }

        user.saveUser();
        return true;
    }
}
