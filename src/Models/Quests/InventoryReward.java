package Models.Quests;

public class InventoryReward extends Reward {
    private String itemId;
    private int quantity;

    public InventoryReward(String itemId, int quantity) {
        super(null);
    }

    public String getItemId() {
        return null;
    }

    public int getQuantity() {
        return 0;
    }

    @Override
    public void grant() {
    }
}
