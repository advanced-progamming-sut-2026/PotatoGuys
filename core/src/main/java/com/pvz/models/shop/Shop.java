package com.pvz.models.shop;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.shop.items.CurrencyExchangeItem;
import com.pvz.models.shop.items.PlantFoodItem;
import com.pvz.models.shop.items.PotSlotItem;
import com.pvz.models.shop.items.RandomSeedPacketItem;
import com.pvz.models.shop.items.SelectableSeedPacketItem;
import com.pvz.models.user.User;

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
        loadOrCreateDailyOffer();
    }

    public boolean buy(int itemId, int count) {
        return buy(itemId, count, null);
    }

    public boolean buy(int itemId, int count, String plantType) {
        if (count <= 0) return false;

        ShopItem item = findItem(itemId);
        if (item == null) return false;

        if (!item.canBuy(currentUser, count, plantType)) return false;

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

    private void loadOrCreateDailyOffer() {
        if (currentUser == null || currentUser.getProfile() == null) {
            createNewDailyOffer();
            return;
        }

        String savedDateStr = currentUser.getProfile().getDailyOfferDate();
        PlantType savedType = currentUser.getProfile().getDailyOfferPlantType();
        boolean purchased = currentUser.getProfile().isDailyOfferPurchased();

        LocalDate savedDate = null;
        if (savedDateStr != null && !savedDateStr.isBlank()) {
            try {
                savedDate = LocalDate.parse(savedDateStr);
            } catch (Exception e) {
                savedDate = null;
            }
        }

        if (savedDate != null && savedDate.equals(LocalDate.now()) && savedType != null) {
            dailyOffer = new DailyOffer(savedType, savedDate);
            dailyOffer.setPurchasedToday(purchased);
        } else {
            createNewDailyOffer();
            saveDailyOfferToProfile();
        }
    }

    private void createNewDailyOffer() {
        List<PlantType> unlocked = new ArrayList<>();
        if (currentUser != null && currentUser.getProfile() != null
                && currentUser.getProfile().getCollection() != null) {
            for (PlantType pt : PlantType.values()) {
                if (currentUser.getProfile().getCollection().getPlant(pt) != null) {
                    unlocked.add(pt);
                }
            }
        }
        if (unlocked.isEmpty()) {
            dailyOffer = null;
            return;
        }

        PlantType randomPlantType = unlocked.get(random.nextInt(unlocked.size()));
        dailyOffer = new DailyOffer(randomPlantType, LocalDate.now());
    }

    private void saveDailyOfferToProfile() {
        if (currentUser == null || currentUser.getProfile() == null || dailyOffer == null) return;

        currentUser.getProfile().setDailyOfferPlantType(dailyOffer.getPlantType());
        currentUser.getProfile().setDailyOfferDate(dailyOffer.getOfferDate().toString());
        currentUser.getProfile().setDailyOfferPurchased(false);
        currentUser.saveUser();
    }
}
