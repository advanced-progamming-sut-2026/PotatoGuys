package pvz.models.games.card;

import pvz.models.engine.TickAware;

public abstract class Card implements TickAware{

    protected final int cost;
    protected final float baseCooldown;
    protected float cooldown;
    protected boolean cooldownEnable;

    protected Card(int cost, float cooldown) {
        this.cost = cost;
        this.baseCooldown = cooldown * 10;
        this.cooldown = baseCooldown;
        cooldownEnable = true;
    }

    public int getCost() {
        return cost;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getBaseCooldown() {
        return baseCooldown;
    }

    public void decrementCooldown() {
        if (cooldown > 0) {
            cooldown -= 1.0f;
        }
    }

    public boolean canUse() {
        if(!cooldownEnable) return true;
        return cooldown == 0;
    }

    public void setCooldown(float cooldown) {
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

    public boolean isCooldownEnable() {
        return cooldownEnable;
    }

    public void setCooldownEnable(boolean cooldownEnable) {
        this.cooldownEnable = cooldownEnable;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void enter() {
        
    }

    @Override
    public void update() {
        decrementCooldown();
    }

}
