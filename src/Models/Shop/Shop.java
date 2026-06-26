package Models.Shop;

import Models.Plants.Enums.PlantType;
import Models.Shop.Items.CurrencyExchangeItem;
import Models.Shop.Items.PlantFoodItem;
import Models.Shop.Items.PotSlotItem;
import Models.Shop.Items.RandomSeedPacketItem;
import Models.Shop.Items.SelectableSeedPacketItem;
import Models.User.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shop {
    private List<ShopItem> permanentItems;
    private DailyOffer dailyOffer;
    private User currentUser;
    private Random random;

    public List<ShopItem> getPermanentItems() {
        return permanentItems;
    }

    public DailyOffer getDailyOffer() {
        return dailyOffer;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Shop(User user) {
        this.currentUser = user;
        this.permanentItems = new ArrayList<>();
        this.random = new Random();
        loadPermanentItems();
        createDailyOffer();
    }

    public boolean buy(int itemId, int count) {
        return buy(itemId, count, null);
    }

    public boolean buy(int itemId, int count, String plantType) {
        if (count <= 0) {
            return false;
        }

        refreshDailyOfferIfNeeded();

        ShopItem item = findItem(itemId);
        if (item == null) {
            return false;
        }

        if (!item.canBuy(currentUser, count, plantType)) {
            return false;
        }

        return item.applyEffect(currentUser, count, plantType);
    }

    private ShopItem findItem(int itemId) {
        for (ShopItem item : permanentItems) {
            if (item.getId() == itemId) {
                return item;
            }
        }

        if (dailyOffer != null && dailyOffer.getId() == itemId) {
            return dailyOffer;
        }

        return null;
    }

    private void loadPermanentItems() {
        permanentItems.add(new PotSlotItem());
        permanentItems.add(new PlantFoodItem());
        permanentItems.add(new RandomSeedPacketItem());
        permanentItems.add(new SelectableSeedPacketItem());
        permanentItems.add(new CurrencyExchangeItem());
    }

    private void createDailyOffer() {
        PlantType[] plantTypes = PlantType.values();
        if (plantTypes.length == 0) {
            dailyOffer = null;
            return;
        }

        PlantType randomPlantType = plantTypes[random.nextInt(plantTypes.length)];
        dailyOffer = new DailyOffer(randomPlantType, LocalDate.now());
    }

    private void refreshDailyOfferIfNeeded() {
        if (dailyOffer == null) {
            createDailyOffer();
            return;
        }

        if (dailyOffer.isExpired(LocalDate.now())) {
            PlantType[] plantTypes = PlantType.values();
            if (plantTypes.length == 0) {
                dailyOffer = null;
                return;
            }

            PlantType randomPlantType = plantTypes[random.nextInt(plantTypes.length)];
            dailyOffer.refresh(randomPlantType);
        }
    }
}
