package pvz.models.quests;

import pvz.models.user.User;

public class InventoryReward extends Reward {
    private String itemId;
    private int quantity;

    public InventoryReward() {}

    public InventoryReward(String itemId, int quantity) {
        super(RewardType.INVENTORY);
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public void grant(User user) {
        if (user == null) return;
        user.getProfile().setPlantFood(user.getProfile().getPlantFood() + quantity);
    }

    @Override
    public String getDescription() {
        return quantity + "x " + itemId;
    }
}
