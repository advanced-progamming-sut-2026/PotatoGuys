package com.pvz.models.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.user.User;

public class InventoryReward extends Reward {
    private String itemId;
    private int quantity;

    public InventoryReward() {}

    public InventoryReward(String itemId, int quantity) {
        super(RewardType.INVENTORY);
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public void grant(User user) {
        if (user == null) return;

        List<PlantType> unlocked = new ArrayList<>();
        for (PlantType pt : PlantType.values()) {
            if (user.getProfile().getCollection().getPlant(pt) != null) {
                unlocked.add(pt);
            }
        }
        if (unlocked.isEmpty()) {
            PlantType fallback = PlantType.values()[ThreadLocalRandom.current().nextInt(PlantType.values().length)];
            user.getProfile().getCollection().addSeedPackets(fallback, quantity);
        } else {
            PlantType target = unlocked.get(ThreadLocalRandom.current().nextInt(unlocked.size()));
            user.getProfile().getCollection().addSeedPackets(target, quantity);
        }
    }

    @Override
    public String getDescription() {
        return quantity + " Seed Packets";
    }
}
