package pvz.Models.Quests;
import pvz.Models.User.User;

public class CurrencyReward extends Reward {
    private CurrencyKind currencyKind;
    private int amount;

    public CurrencyReward(CurrencyKind currencyKind, int amount) {
        super(RewardType.CURRENCY);
        this.currencyKind = currencyKind;
        this.amount = amount;
    }

    @Override
    public void grant(User user) {
        if (currencyKind == CurrencyKind.COIN) {
            // user.addCoins(amount);
        } else if (currencyKind == CurrencyKind.GEM) {
            // user.addDiamonds(amount);
        }
    }
}