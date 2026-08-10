package com.pvz.models.games.levels;

import java.util.List;

import com.pvz.models.entities.zombies.ZombieType;

public class WavePhase {
    private int zombieCount;
    private float intervalSeconds;
    private List<ZombieType> allowedTypes;
    private boolean isBurst;

    public WavePhase(int zombieCount, float intervalSeconds, List<ZombieType> allowedTypes, boolean isBurst) {
        this.setZombieCount(zombieCount);
        this.setIntervalSeconds(intervalSeconds);
        this.setAllowedTypes(allowedTypes);
        this.setBurst(isBurst);
    }

    public WavePhase() {}



    public int getZombieCount() {
        return zombieCount;
    }

    public void setZombieCount(int zombieCount) {
        this.zombieCount = zombieCount;
    }

    public float getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(float intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public List<ZombieType> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<ZombieType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public boolean isBurst() {
        return isBurst;
    }

    public void setBurst(boolean burst) {
        isBurst = burst;
    }
    // getter, setter, constructor
}
