package Models.Quests;

public class InventoryReward extends Reward {
    private String itemId;
    private int quantity;

    
    public InventoryReward(String itemId, int quantity) {
        super(null);
    }
    
    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }
    
    @Override
    public void grant() {
    }
}
