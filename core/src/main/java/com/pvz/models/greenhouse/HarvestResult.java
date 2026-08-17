package com.pvz.models.greenhouse;

public class HarvestResult {

    public enum Type {
        MARIGOLD_COINS,
        NEW_BOOST,
        ALREADY_BOOSTED
    }

    private final Type type;
    private final String plantTypeName;
    private final int coins;

    private HarvestResult(Type type, String plantTypeName, int coins) {
        this.type = type;
        this.plantTypeName = plantTypeName;
        this.coins = coins;
    }

    public static HarvestResult marigoldCoins(int coins) {
        return new HarvestResult(Type.MARIGOLD_COINS, "MariGold", coins);
    }

    public static HarvestResult newBoost(String plantTypeName) {
        return new HarvestResult(Type.NEW_BOOST, plantTypeName, 0);
    }

    public static HarvestResult alreadyBoosted(String plantTypeName) {
        return new HarvestResult(Type.ALREADY_BOOSTED, plantTypeName, 0);
    }

    public Type getType() {
        return type;
    }

    public String getPlantTypeName() {
        return plantTypeName;
    }

    public int getCoins() {
        return coins;
    }
}
