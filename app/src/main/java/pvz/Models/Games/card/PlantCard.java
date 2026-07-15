package pvz.Models.Games.card;

import pvz.Models.User.MyPlant;

public class PlantCard extends Card {
    private final MyPlant plant;

    public PlantCard(MyPlant plant, int cost, int cooldown) {
        super(cost, cooldown);
        this.plant = plant;
    }

    public MyPlant getPlant() {
        return plant;
    }

    public MyPlant use() {
        if (canUse()) {
            resetCooldown();
            return plant;
        }
        return null;
    }
}
