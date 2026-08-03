package com.pvz.models.entities.projectile;

public enum ProjectileType {
    PEA(0.25f),
    SNOW_PEA(0.25f),
    FIRE_PEA(0.25f),
    GOO_PEA(0.25f),
    SPIKE(0.35f),
    FUME(0.20f),
    SPORE(0.20f),
    STAR(0.25f),
    ROTOBAGA_PROJECTILE(0.25f),
    CITRON_BALL(0.30f),
    BULB_CYAN(0.22f),
    BULB_BLUE(0.22f),
    HOMING_BOLT(0.22f),
    BULB_ORANGE(0.22f);

    private final float speed; // سرعت بر حسب کاشی (Grid Cell) در هر تیک

    ProjectileType(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }
}
