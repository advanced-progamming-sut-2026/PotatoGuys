package com.pvz.models.user;

import com.pvz.models.entities.plants.enums.PlantType;

public class MyPlant {
    private PlantType Type;
    private int level;
    private int seed;
    private boolean isBoosted;


    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }


    public PlantType getType() {
        return Type;
    }

    public void setType(PlantType type) {
        this.Type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isBoosted() {
        return isBoosted;
    }

    public void setBoosted(boolean boosted) {
        isBoosted = boosted;
    }
}
