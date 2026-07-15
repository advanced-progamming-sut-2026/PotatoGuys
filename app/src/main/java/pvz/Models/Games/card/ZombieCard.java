package pvz.Models.Games.card;

import pvz.Models.Entities.Zombies.ZombieType;

public class ZombieCard extends Card {
    private final ZombieType zombieType;

    public ZombieCard(ZombieType zombieType, int cost, int cooldown) {
        super(cost, cooldown);
        this.zombieType = zombieType;
    }

    public ZombieType getZombieType() {
        return zombieType;
    }
    
    public ZombieType use() {
        if (canUse()) {
            resetCooldown();
            return zombieType;
        }
        return null;
    }
}
