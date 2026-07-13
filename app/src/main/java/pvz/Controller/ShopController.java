package pvz.Controller;

import pvz.Models.User.User;
import pvz.Models.Shop.ShopItem;
// Import your specific ShopItems here

public class ShopController {
    private User currentUser;

    public ShopController(User currentUser) {
        this.currentUser = currentUser;
    }

    public String showPermanentItems() {
        // In a full implementation, you would iterate over a static list of available ShopItems
        return "1: Pot (2000 Coins)\n2: Plant Food (3 Diamonds)\n3: Random Seed Packet (1000 Coins)\n4: Selectable Seed Packet (5 Diamonds)\n5: Currency Exchange (5 Diamonds for 500 Coins)";
    }

    public String showDailyOffer() {
        // Logic to generate and display the daily offer (1600 coins for 10 seed packets)
        // This should check the system date to ensure it only changes at 00:00
        return "Daily Offer: 10x Random Seed Packets for 1600 Coins!";
    }

    public String buyItem(int itemId, int count, String plantType) {
        if (currentUser == null) return "No user logged in.";

        ShopItem item = getShopItemById(itemId); // Helper method to instantiate the correct item
        if (item == null) return "Invalid item ID.";

        if (!item.canBuy(currentUser, count, plantType)) {
            return "Cannot buy this item (insufficient funds, max capacity reached, or invalid plant type).";
        }

        boolean success = item.applyEffect(currentUser, count, plantType);
        if (success) {
            return "Successfully purchased " + count + "x " + item.getName();
        } else {
            return "Purchase failed.";
        }
    }

    private ShopItem getShopItemById(int id) {
        // Simple factory logic to return the correct item model
        // e.g., if (id == 1) return new PotSlotItem();
        return null;
    }
}