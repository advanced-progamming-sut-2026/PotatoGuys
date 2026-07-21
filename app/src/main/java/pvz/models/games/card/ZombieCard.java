package pvz.models.games.card;

import pvz.models.entities.zombies.ZombieType;

public class ZombieCard extends Card {
    private final ZombieType zombieType;

    public ZombieCard(ZombieType zombieType, int cost, float cooldown) {
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
