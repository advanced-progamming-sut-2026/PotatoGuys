package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Shop.DailyOffer;
import pvz.Models.Shop.Shop;
import pvz.Models.Shop.ShopItem;
import pvz.Models.User.User;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.Result;

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
        String plantType = matcher.group(3);

        Shop shop = new Shop(currentUser);
        ShopItem item = findItem(shop, itemId);
        if (item == null) {
            return new Result("Invalid item ID.");
        }

        if (item instanceof pvz.Models.Shop.Items.SelectableSeedPacketItem) {
            if (plantType == null || plantType.isBlank()) {
                return new Result("For Selectable Seed Packet, the -t parameter is mandatory.");
            }
            PlantType selectedType = null;
            for (PlantType pt : PlantType.values()) {
                if (pt.name().equalsIgnoreCase(plantType)) {
                    selectedType = pt;
                    break;
                }
            }
            if (selectedType == null) {
                return new Result("Invalid plant type: " + plantType);
            }
            if (currentUser.getProfile().getCollection().getPlant(selectedType) == null) {
                return new Result("Plant " + plantType + " is not unlocked yet.");
            }
        }

        if (!item.canBuy(currentUser, count, plantType)) {
            if (currentUser.getProfile().getCoins() < item.getPrice().getAmount() * count
                    && item.getPrice().getCurrency() == pvz.Models.Shop.Currency.COIN) {
                return new Result("Insufficient coins. Need " + (item.getPrice().getAmount() * count)
                        + " coins, have " + currentUser.getProfile().getCoins() + ".");
            }
            if (currentUser.getProfile().getDiamonds() < item.getPrice().getAmount() * count
                    && item.getPrice().getCurrency() == pvz.Models.Shop.Currency.DIAMOND) {
                return new Result("Insufficient diamonds. Need " + (item.getPrice().getAmount() * count)
                        + " diamonds, have " + currentUser.getProfile().getDiamonds() + ".");
            }
            return new Result("Cannot buy this item (capacity limit reached or invalid parameters).");
        }

        boolean success = item.applyEffect(currentUser, count, plantType);
        if (success) {
            return new Result("Successfully purchased " + item.getName() + " x" + count + ".");
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
