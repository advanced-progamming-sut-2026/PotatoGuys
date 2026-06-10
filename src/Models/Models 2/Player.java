import java.util.HashMap;
import java.util.Map;

public class Player {

    private int coins;
    private int diamonds;

    private Map<String, Boolean> boosts = new HashMap<>();

    public void addCoins(int amount){
        coins += amount;
    }

    public boolean spendCoins(int amount){

        if(coins < amount)
            return false;

        coins -= amount;
        return true;
    }

}
