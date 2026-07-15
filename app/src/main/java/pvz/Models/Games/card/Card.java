package pvz.Models.Games.card;


public abstract class Card {

    protected final int cost;
    protected final int baseColldown;
    protected int cooldown;

    protected Card(int cost, int cooldown) {
        this.cost = cost;
        this.baseColldown = cooldown;
        this.cooldown = cooldown;
    }

    public int getCost() {
        return cost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getBaseColldown() {
        return baseColldown;
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
        } else if (cooldown > baseColldown) {
            this.cooldown = baseColldown;
        } else {
            this.cooldown = cooldown;
        }
    }

    public void resetCooldown() {
        this.cooldown = baseColldown;
    }

}
