package pvz.models.games.card;

import pvz.models.user.MyPlant;

public class PlantCard extends Card {
    private final MyPlant plant;

    public PlantCard(MyPlant plant, int cost, float cooldown) {
        super(cost, cooldown);
        this.plant = plant;
    }

    public MyPlant getPlant() {
        return plant;
    }

    public MyPlant use() {
        if (canUse()) {
            resetCooldown();
            plant.setBoosted(false);
            return plant;
        }
        return null;
    }
}
