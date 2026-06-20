package Models.Quests;

public class CurrencyReward extends Reward {
    private CurrencyKind currencyKind;
    private int amount;

    public CurrencyReward(CurrencyKind currencyKind, int amount) {
        super(null);
    }

    public CurrencyKind getCurrencyKind() {
        return currencyKind;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void grant() {
    }
}
