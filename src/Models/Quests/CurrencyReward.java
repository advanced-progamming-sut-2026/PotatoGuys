package Models.Quests;

public class CurrencyReward extends Reward {
    private CurrencyKind currencyKind;
    private int amount;

    public CurrencyReward(CurrencyKind currencyKind, int amount) {
        super(null);
    }

    public CurrencyKind getCurrencyKind() {
        return null;
    }

    public int getAmount() {
        return 0;
    }

    @Override
    public void grant() {
    }
}
