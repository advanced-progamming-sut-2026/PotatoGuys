package pvz.controller;

import java.util.regex.Matcher;

import pvz.models.AppContext;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.shop.DailyOffer;
import pvz.models.shop.Shop;
import pvz.models.shop.ShopItem;
import pvz.models.user.User;
import pvz.view.MainMenu;
import pvz.view.Menu;
import pvz.view.Result;

public class ShopController {
    private Menu previousMenu;

    public ShopController() {
        this.previousMenu = new MainMenu();
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

        // Lifted to be accessible after applyEffect
        PlantType selectedType = null;

        if (item instanceof pvz.models.shop.items.SelectableSeedPacketItem) {
            if (plantTypeStr == null || plantTypeStr.isBlank()) {
                return new Result("For Selectable Seed Packet, the -t parameter is mandatory.");
            }
            for (PlantType pt : PlantType.values()) {
                if (pt.name().equalsIgnoreCase(plantTypeStr)) {
                    selectedType = pt;
                    break;
                }
            }
            if (selectedType == null) {
                return new Result("Invalid plant type: " + plantTypeStr);
            }
            if (currentUser.getProfile().getCollection().getPlant(selectedType) == null) {
                return new Result("Plant " + plantTypeStr + " is not unlocked yet.");
            }
        }

        if (!item.canBuy(currentUser, count, plantTypeStr)) {
            if (currentUser.getProfile().getCoins() < item.getPrice().getAmount() * count
                    && item.getPrice().getCurrency() == pvz.models.shop.Currency.COIN) {
                return new Result("Insufficient coins. Need " + (item.getPrice().getAmount() * count)
                        + " coins, have " + currentUser.getProfile().getCoins() + ".");
            }
            if (currentUser.getProfile().getDiamonds() < item.getPrice().getAmount() * count
                    && item.getPrice().getCurrency() == pvz.models.shop.Currency.DIAMOND) {
                return new Result("Insufficient diamonds. Need " + (item.getPrice().getAmount() * count)
                        + " diamonds, have " + currentUser.getProfile().getDiamonds() + ".");
            }
            return new Result("Cannot buy this item (capacity limit reached or invalid parameters).");
        }

        boolean success = item.applyEffect(currentUser, count, plantTypeStr);
        if (success) {
            // Explicitly force the seed packets into the collection upon purchase
            if (selectedType != null) {
                int totalPackets = item.getUnitAmount() * count;
                currentUser.getProfile().getCollection().addSeedPackets(selectedType, totalPackets);
                currentUser.saveUser();
            }
            String msg = "Successfully purchased " + item.getName() + " x" + count + ".";
            if (item instanceof pvz.models.shop.items.RandomSeedPacketItem) {
                String details = ((pvz.models.shop.items.RandomSeedPacketItem) item).getLastPurchaseDetails();
                if (details != null) {
                    msg += " Seeds awarded: " + details;
                }
            }
            return new Result(msg);
        } else {
            return new Result("Purchase failed.");
        }
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
}
