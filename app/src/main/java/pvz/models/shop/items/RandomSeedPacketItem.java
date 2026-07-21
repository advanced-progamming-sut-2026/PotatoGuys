package pvz.models.shop.items;

import java.util.List;
import java.util.Random;

import pvz.models.shop.Currency;
import pvz.models.shop.Price;
import pvz.models.shop.ShopItem;
import pvz.models.user.MyPlant;
import pvz.models.user.User;

public class RandomSeedPacketItem extends ShopItem {
    private final Random random = new Random();

    public RandomSeedPacketItem() {
        this.id = 3;
        this.name = "Random Seed Packet";
        this.price = new Price(Currency.COIN, 1000);
        this.unitAmount = 5;
        this.maxPurchasePerUser = 0;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        if (user == null || count <= 0) return false;

        List<MyPlant> unlocked = user.getProfile().getCollection().getUnlockedPlants();
        if (unlocked == null || unlocked.isEmpty()) return false;

        int totalCost = price.getAmount() * count;
        if (user.getProfile().getCoins() < totalCost) return false;

        return true;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        int totalCost = price.getAmount() * count;
        user.getProfile().setCoins(user.getProfile().getCoins() - totalCost);

        List<MyPlant> unlocked = user.getProfile().getCollection().getUnlockedPlants();
        int totalPackets = unitAmount * count;

        for (int i = 0; i < totalPackets; i++) {
            int idx = random.nextInt(unlocked.size());
            MyPlant target = unlocked.get(idx);
            target.setSeed(target.getSeed() + 5);
        }

        user.saveUser();
        return true;
    }
}
