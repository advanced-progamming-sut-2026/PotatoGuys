package pvz.models.shop.items;

import java.util.Random;

import pvz.models.entities.plants.enums.PlantType;
import pvz.models.shop.Currency;
import pvz.models.shop.Price;
import pvz.models.shop.ShopItem;
import pvz.models.user.User;

public class RandomSeedPacketItem extends ShopItem {
    private final Random random = new Random();
    private String lastPurchaseDetails;

    public RandomSeedPacketItem() {
        this.id = 3;
        this.name = "Random Seed Packet";
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
        return user.getProfile().getCoins() >= totalCost;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        int totalCost = price.getAmount() * count;
        user.getProfile().setCoins(user.getProfile().getCoins() - totalCost);

        PlantType[] allPlants = PlantType.values();
        PlantType target = allPlants[random.nextInt(allPlants.length)];
        int totalSeeds = unitAmount * count;

        user.getProfile().getCollection().addSeedPackets(target, totalSeeds);
        lastPurchaseDetails = target.name() + " +" + totalSeeds;

        user.saveUser();
        return true;
    }
}
