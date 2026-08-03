package com.pvz.controller;

import java.util.regex.Matcher;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.shop.DailyOffer;
import com.pvz.models.shop.Shop;
import com.pvz.models.shop.ShopItem;
import com.pvz.models.user.User;

public class ShopController {
/*
    private Menu previousMenu;

    public ShopController() {
        this.previousMenu = new pvz.view.OldMainMenu();
    }

    public ShopController(Menu previousMenu) {
        this.previousMenu = previousMenu;
    }

    private User getCurrentUser() {
        return AppContext.getInstance().getCurrentUser();
    }

    public Result showPermanentItems(Matcher matcher) {
        Shop shop = new Shop(getCurrentUser());
        StringBuilder sb = new StringBuilder("Permanent Items:");
        for (ShopItem item : shop.getPermanentItems()) {
            sb.append("\n").append(item.getId()).append(": ").append(item.getName())
                    .append(" - ").append(item.getPrice().getAmount()).append(" ")
                    .append(item.getPrice().getCurrency())
                    .append(" | Unit: ").append(item.getUnitAmount());
        }
        return new Result(sb.toString());
    }

    public Result showDailyOffer(Matcher matcher) {
        Shop shop = new Shop(getCurrentUser());
        DailyOffer offer = shop.getDailyOffer();
        if (offer == null) {
            return new Result("No daily offer available today.");
        }
        StringBuilder sb = new StringBuilder("Daily Offer:");
        sb.append("\nPlant: ").append(offer.getPlantType().name());
        sb.append("\nPackets: ").append(offer.getUnitAmount());
        sb.append("\nPrice: ").append(offer.getPrice().getAmount()).append(" ").append(offer.getPrice().getCurrency());
        sb.append(" (20% off from 2000 Coins)");
        if (offer.isPurchasedToday()) {
            sb.append("\nStatus: Already purchased today.");
        } else {
            sb.append("\nStatus: Available.");
        }
        return new Result(sb.toString());
    }

    public Result buyItem(Matcher matcher) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new Result("No user logged in.");
        }

        int itemId = Integer.parseInt(matcher.group(1));
        int count = Integer.parseInt(matcher.group(2));
        String plantTypeStr = matcher.group(3);

        Shop shop = new Shop(currentUser);
        ShopItem item = findItem(shop, itemId);
        if (item == null) {
            return new Result("Invalid item ID.");
        }

        PlantType selectedType = validateAndGetPlantType(item, currentUser, plantTypeStr);
        if (selectedType == null && item instanceof pvz.models.shop.items.SelectableSeedPacketItem
                && (plantTypeStr == null || plantTypeStr.isBlank())) {
            return new Result("For Selectable Seed Packet, the -t parameter is mandatory.");
        }
        if (selectedType == null && item instanceof pvz.models.shop.items.SelectableSeedPacketItem) {
            // If validation failed for selectable item, return error
            if (findPlantType(plantTypeStr) == null) {
                return new Result("Invalid plant type: " + plantTypeStr);
            }
            return new Result("Plant " + plantTypeStr + " is not unlocked yet.");
        }

        Result purchaseCheckResult = checkAffordabilityAndLimits(item, currentUser, count, plantTypeStr);
        if (purchaseCheckResult != null) {
            return purchaseCheckResult;
        }

        return executePurchase(item, currentUser, count, plantTypeStr, selectedType);
    }

    private PlantType findPlantType(String plantTypeStr) {
        if (plantTypeStr == null || plantTypeStr.isBlank()) {
            return null;
        }
        for (PlantType pt : PlantType.values()) {
            if (pt.name().equalsIgnoreCase(plantTypeStr)) {
                return pt;
            }
        }
        return null;
    }

    private PlantType validateAndGetPlantType(ShopItem item, User currentUser, String plantTypeStr) {
        if (!(item instanceof pvz.models.shop.items.SelectableSeedPacketItem)) {
            return null;
        }
        if (plantTypeStr == null || plantTypeStr.isBlank()) {
            return null;
        }
        PlantType selectedType = findPlantType(plantTypeStr);
        if (selectedType == null) {
            return null;
        }
        if (currentUser.getProfile().getCollection().getPlant(selectedType) == null) {
            return null;
        }
        return selectedType;
    }

    private Result checkAffordabilityAndLimits(ShopItem item, User currentUser, int count, String plantTypeStr) {
        if (!item.canBuy(currentUser, count, plantTypeStr)) {
            long totalPrice = (long) item.getPrice().getAmount() * count;
            if (currentUser.getProfile().getCoins() < totalPrice
                    && item.getPrice().getCurrency() == pvz.models.shop.Currency.COIN) {
                return new Result("Insufficient coins. Need " + totalPrice
                        + " coins, have " + currentUser.getProfile().getCoins() + ".");
            }
            if (currentUser.getProfile().getDiamonds() < totalPrice
                    && item.getPrice().getCurrency() == pvz.models.shop.Currency.DIAMOND) {
                return new Result("Insufficient diamonds. Need " + totalPrice
                        + " diamonds, have " + currentUser.getProfile().getDiamonds() + ".");
            }
            return new Result("Cannot buy this item (capacity limit reached or invalid parameters).");
        }
        return null;
    }

    private Result executePurchase(ShopItem item, User currentUser, int count, String plantTypeStr,
            PlantType selectedType) {
        boolean success = item.applyEffect(currentUser, count, plantTypeStr);
        if (!success) {
            return new Result("Purchase failed.");
        }

        if (selectedType != null) {
            int totalPackets = item.getUnitAmount() * count;
            currentUser.getProfile().getCollection().addSeedPackets(selectedType, totalPackets);
            currentUser.saveUser();
        }
        String msg = "Successfully purchased " + item.getName() + " x" + count + ".";
        if (item instanceof pvz.models.shop.items.RandomSeedPacketItem randomItem) {
            String details = randomItem.getLastPurchaseDetails();
            if (details != null) {
                msg += " Seeds awarded: " + details;
            }
        }
        return new Result(msg);
    }

    public Result exit(Matcher matcher) {
        return new Result("Exited to " + previousMenu.getName(), previousMenu);
    }

    private ShopItem findItem(Shop shop, int itemId) {
        for (ShopItem item : shop.getPermanentItems()) {
            if (item.getId() == itemId) {
                return item;
            }
        }
        if (shop.getDailyOffer() != null && shop.getDailyOffer().getId() == itemId) {
            return shop.getDailyOffer();
        }
        return null;
    }
*/
}
