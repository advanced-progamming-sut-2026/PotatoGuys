package com.pvz.models.entities.plants.config;

public class NutConfig extends PlantActionConfig{
    public String idleClip="idle";
    public String damage1Clip="damage";
    public String damage2Clip="damage2";
    public String damage3Clip="damage3";
    public float baseDamage=1800f;

    /** Explode-o-nut: detonates when destroyed (routes to ExplodeONutAction). */
    public boolean explodesOnDestroy=false;
    /** Endurian: damage reflected back onto zombies chewing the plant. */
    public float reflectDamage=0f;
    /** Sun Bean: sun amount dropped per bite while being eaten. */
    public int sunOnHit=0;
    /** Garlic: forces any eater to an adjacent lane. */
    public boolean redirectEaters=false;
    /** Sweet Potato: continuously pulls nearby zombies into its lane. */
    public boolean attractAdjacent=false;
}
