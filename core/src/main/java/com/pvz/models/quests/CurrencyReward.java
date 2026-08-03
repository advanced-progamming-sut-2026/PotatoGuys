package com.pvz.models.quests;

import com.pvz.models.user.User;

public class CurrencyReward extends Reward {
    private CurrencyKind currencyKind;
    private int amount;

    public CurrencyReward() {}

    public CurrencyReward(CurrencyKind currencyKind, int amount) {
        super(RewardType.CURRENCY);
        this.currencyKind = currencyKind;
        this.amount = amount;
    }

    public CurrencyKind getCurrencyKind() {
        return currencyKind;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void grant(User user) {
        if (user == null) return;
        if (currencyKind == CurrencyKind.COIN) {
            user.getProfile().addCoins(amount);
        } else if (currencyKind == CurrencyKind.GEM) {
            user.getProfile().addDiamonds(amount);
        }
    }

    @Override
    public String getDescription() {
        return amount + " " + currencyKind;
    }
}
