package com.pvz.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.shop.Currency;
import com.pvz.models.shop.DailyOffer;
import com.pvz.models.shop.Shop;
import com.pvz.models.shop.ShopItem;
import com.pvz.models.shop.items.PotSlotItem;
import com.pvz.models.shop.items.SelectableSeedPacketItem;
import com.pvz.models.user.Collection;
import com.pvz.models.user.User;

/**
 * Live controller for the graphical shop. Rebuilt from scratch — the old
 * ShopController was fully commented-out console-era code (Result/Matcher based),
 * but its validation logic and error messages were solid, so this keeps that
 * same behavior/wording, just as plain method calls instead of regex commands.
 */
public class ShopController {

    private Shop shop;

    private User getCurrentUser() {
        return AppContext.getInstance().getCurrentUser();
    }

    /** Lazily creates the shop once per controller instance (Shop's constructor already
     *  handles loading/creating today's daily offer idempotently from the user's profile). */
    public Shop getShop() {
        if (shop == null) {
            shop = new Shop(getCurrentUser());
        }
        return shop;
    }

    public List<ShopItem> getPermanentItems() {
        return getShop().getPermanentItems();
    }

    public DailyOffer getDailyOffer() {
        return getShop().getDailyOffer();
    }

    /** Plants the user actually owns — for the Selectable Seed Packet's plant picker. */
    public List<PlantType> getUnlockedPlantTypes() {
        List<PlantType> result = new ArrayList<>();
        User user = getCurrentUser();
        if (user == null) {
            return result;
        }
        Collection collection = user.getProfile().getCollection();
        for (PlantType type : PlantType.values()) {
            if (collection.getPlant(type) != null) {
                result.add(type);
            }
        }
        return result;
    }

    /** How many of maxPurchasePerUser this item has left this session, or -1 if unlimited (0). */
    public int getRemainingCapacity(ShopItem item) {
        if (item.getMaxPurchasePerUser() <= 0) {
            return -1; // unlimited
        }
        User user = getCurrentUser();
        if (user == null) {
            return item.getMaxPurchasePerUser();
        }
        if (item instanceof com.pvz.models.shop.items.PlantFoodItem) {
            return Math.max(0, item.getMaxPurchasePerUser() - user.getProfile().getPlantFood());
        }
        if (item instanceof PotSlotItem) {
            return getGreenHouse().getLockedPotCount();
        }
        return item.getMaxPurchasePerUser();
    }

    private GreenHouse getGreenHouse() {
        User user = getCurrentUser();
        GreenHouse gh = AppContext.getInstance().getGreenHouse();
        if (gh == null && user != null) {
            gh = user.getGreenHouse();
        }
        if (gh == null) {
            gh = new GreenHouse();
        }
        if (user != null) {
            user.setGreenHouse(gh);
        }
        AppContext.getInstance().setGreenHouse(gh);
        return gh;
    }

    /** How long until the daily offer resets (next local midnight). Null if there's no offer. */
    public Duration getDailyOfferTimeRemaining() {
        DailyOffer offer = getDailyOffer();
        if (offer == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT);
        return Duration.between(now, nextMidnight);
    }

    /**
     * Attempts a purchase. Returns null on success, or a specific, user-facing error
     * message on failure — never throws, always safe to show directly in the UI.
     */
    public String purchase(ShopItem item, int count, PlantType plantType) {
        User user = getCurrentUser();
        if (user == null) {
            return "No user logged in.";
        }
        if (item == null) {
            return "Invalid item.";
        }
        if (count <= 0) {
            return "Invalid quantity.";
        }

        if (item instanceof SelectableSeedPacketItem) {
            if (plantType == null) {
                return "Select a plant first.";
            }
            if (user.getProfile().getCollection().getPlant(plantType) == null) {
                return plantType.name() + " is not unlocked yet.";
            }
        }

        String plantTypeParam = plantType != null ? plantType.name() : null;

        if (!item.canBuy(user, count, plantTypeParam)) {
            long totalPrice = (long) item.getPrice().getAmount() * count;
            if (item.getPrice().getCurrency() == Currency.COIN && user.getProfile().getCoins() < totalPrice) {
                return "Insufficient coins. Need " + totalPrice + ", have " + user.getProfile().getCoins() + ".";
            }
            if (item.getPrice().getCurrency() == Currency.DIAMOND && user.getProfile().getDiamonds() < totalPrice) {
                return "Insufficient diamonds. Need " + totalPrice + ", have " + user.getProfile().getDiamonds() + ".";
            }
            if (item instanceof DailyOffer dailyOffer && dailyOffer.isPurchasedToday()) {
                return "You already bought today's offer. Come back tomorrow.";
            }
            return "You've reached the limit for this item.";
        }

        boolean success = item.applyEffect(user, count, plantTypeParam);
        return success ? null : "Purchase failed.";
    }
}
