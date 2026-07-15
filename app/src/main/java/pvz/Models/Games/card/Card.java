package pvz.Models.Games.card;


public abstract class Card {

    protected final int cost;
    protected final float baseCooldown;
    protected float cooldown;

    protected Card(int cost, float cooldown) {
        this.cost = cost;
        this.baseCooldown = cooldown;
        this.cooldown = cooldown;
    }

    public int getCost() {
        return cost;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getBaseColldown() {
        return baseCooldown;
    }

    public void decrementCooldown() {
        if (cooldown > 0) {
            cooldown--;
        }
    }

    public boolean canUse() {
        return cooldown == 0;
    }

    public void setCooldown(int cooldown) {
        if (cooldown < 0) {
            this.cooldown = 0;
        } else if (cooldown > baseCooldown) {
            this.cooldown = baseCooldown;
        } else {
            this.cooldown = cooldown;
        }
    }

    public void resetCooldown() {
        this.cooldown = baseCooldown;
    }

}
