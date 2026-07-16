package pvz.Models.Shop.Items;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Shop.Currency;
import pvz.Models.Shop.Price;
import pvz.Models.Shop.ShopItem;
import pvz.Models.User.MyPlant;
import pvz.Models.User.User;

public class SelectableSeedPacketItem extends ShopItem {

    public SelectableSeedPacketItem() {
        this.id = 4;
        this.name = "Selectable Seed Packet";
        this.price = new Price(Currency.DIAMOND, 5);
        this.unitAmount = 10;
        this.maxPurchasePerUser = 0;
    }

    @Override
    public boolean canBuy(User user, int count, String plantType) {
        if (user == null || count <= 0) return false;
        if (plantType == null || plantType.isBlank()) return false;

        PlantType selectedType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.name().equalsIgnoreCase(plantType)) {
                selectedType = pt;
                break;
            }
        }
        if (selectedType == null) return false;

        MyPlant owned = user.getProfile().getCollection().getPlant(selectedType);
        if (owned == null) return false;

        int totalCost = price.getAmount() * count;
        if (user.getProfile().getDiamonds() < totalCost) return false;

        return true;
    }

    @Override
    public boolean applyEffect(User user, int count, String plantType) {
        if (!canBuy(user, count, plantType)) return false;

        int totalCost = price.getAmount() * count;
        user.getProfile().setDiamonds(user.getProfile().getDiamonds() - totalCost);

        PlantType selectedType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.name().equalsIgnoreCase(plantType)) {
                selectedType = pt;
                break;
            }
        }

        MyPlant target = user.getProfile().getCollection().getPlant(selectedType);
        int totalPackets = unitAmount * count;
        target.setSeed(target.getSeed() + totalPackets);

        user.saveUser();
        return true;
    }
}
