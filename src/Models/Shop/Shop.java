package Models.Shop;
import Models.User.User;

import java.util.List;

public class Shop {
    private List<ShopItem> permanentItems;
    private DailyOffer dailyOffer;
    private User currentUser;

    public List<ShopItem> getPermanentItems() {
        return permanentItems;
    }

    public DailyOffer getDailyOffer() {
        return dailyOffer;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Shop(User user){

    }

    public void buy(int itemId, int count){

    }

    private ShopItem findItem(int ItemId){
        return null;
    }
    private void loadPermanentItems() {

    }
}
