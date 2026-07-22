package pvz.models.shop.items;

import pvz.models.shop.Currency;
import pvz.models.shop.Price;
import pvz.models.shop.ShopItem;
import pvz.models.user.User;

public class PlantFoodItem extends ShopItem {

    public PlantFoodItem() {
        this.id = 2;
        this.name = "Plant Food";
        this.price = new Price(Currency.DIAMOND, 3);
        this.unitAmount = 1;
        this.maxPurchasePerUser = 3;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        if (user == null || count <= 0) return false;

        int totalCost = price.getAmount() * count;
        if (user.getProfile().getDiamonds() < totalCost) return false;

        int current = user.getProfile().getPlantFood();
        if (current + count > maxPurchasePerUser) return false;

        return true;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        int totalCost = price.getAmount() * count;
        user.getProfile().setDiamonds(user.getProfile().getDiamonds() - totalCost);
        user.getProfile().setPlantFood(user.getProfile().getPlantFood() + count);

        user.saveUser();
        return true;
    }
}
