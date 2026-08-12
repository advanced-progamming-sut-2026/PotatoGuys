package com.pvz.models.shop.items;

import java.util.List;
import java.util.Random;

import com.pvz.models.shop.Currency;
import com.pvz.models.shop.Price;
import com.pvz.models.shop.ShopItem;
import com.pvz.models.user.MyPlant;
import com.pvz.models.user.User;

public class RandomSeedPacketItem extends ShopItem {
    private final Random random = new Random();
    private String lastPurchaseDetails;

    public RandomSeedPacketItem() {
        this.id = 3;
        this.name = "Random Seed Packet";
        this.description = "Grants   seed   packets   for   a   random plant.";
        this.price = new Price(Currency.COIN, 1000);
        this.unitAmount = 10;
        this.maxPurchasePerUser = 0;
    }

    public String getLastPurchaseDetails() {
        return lastPurchaseDetails;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        if (user == null || count <= 0) return false;
        int totalCost = price.getAmount() * count;
        if (user.getProfile().getCoins() < totalCost) return false;

        return !user.getProfile().getCollection().getUnlockedPlants().isEmpty();
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        List<MyPlant> unlocked = user.getProfile().getCollection().getUnlockedPlants();
        if (unlocked.isEmpty()) return false;

        int totalCost = price.getAmount() * count;
        user.getProfile().setCoins(user.getProfile().getCoins() - totalCost);

        MyPlant chosen = unlocked.get(random.nextInt(unlocked.size()));
        int totalSeeds = unitAmount * count;

        user.getProfile().getCollection().addSeedPackets(chosen.getType(), totalSeeds);
        lastPurchaseDetails = chosen.getType().name() + " +" + totalSeeds;

        user.saveUser();
        return true;
    }
}
