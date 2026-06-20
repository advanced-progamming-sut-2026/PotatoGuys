package Models.Shop;

public class Price {
    private Currency currency;
    private int amount;

    public Price(Currency currencyType, int amount){
        this.currency=currencyType;
        this.amount=amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public int getAmount() {
        return amount;
    }
}
