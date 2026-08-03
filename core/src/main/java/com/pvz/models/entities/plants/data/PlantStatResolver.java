package com.pvz.models.entities.plants.data;

import java.util.ArrayList;
import java.util.List;

public final class PlantStatResolver {

    private PlantStatResolver() { }

    public static ResolvedStats resolve(PlantPropertySheet sheet, int level) {
        float maxHp = sheet.getBaseHp();
        float damage = sheet.getDamage().getValue();
        float sunCost = sheet.getSunCost();
        Float actionInterval = sheet.getActionIntervalSeconds();
        Float recharge = sheet.getRechargeSeconds();
        int rangeTiles = 0;
        int pierceCount = 0;
        float atkSpeedPercent = 0f;
        float effectDurationBonus = 0f;
        List<String> flags = new ArrayList<>();

        for (LevelUpgrade upgrade : sheet.getLevelUpgrades()) {
            if (upgrade.getLevel() > level) continue;
            for (StatModifier mod : upgrade.getStats()) {
                switch (mod.getStat()) {
                    case MAX_HP -> maxHp += mod.getDelta();
                    case DAMAGE -> damage += mod.getDelta();
                    case SUN_COST -> sunCost += mod.getDelta();
                    case ACTION_INTERVAL_SECONDS ->
                            actionInterval = (actionInterval == null ? 0f : actionInterval) + mod.getDelta();
                    case RECHARGE_SECONDS ->
                            recharge = (recharge == null ? 0f : recharge) + mod.getDelta();
                    case RANGE_TILES -> rangeTiles += (int) mod.getDelta();
                    case PIERCE_COUNT -> pierceCount += (int) mod.getDelta();
                    case ATK_SPEED_PERCENT -> atkSpeedPercent += mod.getDelta();
                    case EFFECT_DURATION_SECONDS -> effectDurationBonus += mod.getDelta();
                }
            }
            flags.addAll(upgrade.getFlags());
        }

        if (actionInterval != null && atkSpeedPercent != 0f) {
            actionInterval = actionInterval * (1f - atkSpeedPercent / 100f);
        }

        return new ResolvedStats(
                Math.max(0f, maxHp),
                Math.max(0f, damage),
                Math.max(0, Math.round(sunCost)),
                clampPositiveOrNull(actionInterval),
                clampPositiveOrNull(recharge),
                Math.max(0, rangeTiles),
                Math.max(0, pierceCount),
                Math.max(0f, effectDurationBonus),
                List.copyOf(flags));
    }

    private static Float clampPositiveOrNull(Float v) {
        if (v == null) return null;
        return Math.max(0.05f, v);
    }

    /** Immutable bag of stats resolved for a specific plant level. */
    public static final class ResolvedStats {
        private final float maxHp;
        private final float damage;
        private final int sunCost;
        private final Float actionIntervalSeconds;
        private final Float rechargeSeconds;
        private final int rangeTiles;
        private final int pierceCount;
        private final float effectDurationBonusSeconds;
        private final List<String> unlockedFlags;

        ResolvedStats(float maxHp, float damage, int sunCost, Float actionIntervalSeconds,
                      Float rechargeSeconds, int rangeTiles, int pierceCount,
                      float effectDurationBonusSeconds, List<String> unlockedFlags) {
            this.maxHp = maxHp;
            this.damage = damage;
            this.sunCost = sunCost;
            this.actionIntervalSeconds = actionIntervalSeconds;
            this.rechargeSeconds = rechargeSeconds;
            this.rangeTiles = rangeTiles;
            this.pierceCount = pierceCount;
            this.effectDurationBonusSeconds = effectDurationBonusSeconds;
            this.unlockedFlags = unlockedFlags;
        }

        public float getMaxHp()                       { return maxHp; }
        public float getDamage()                      { return damage; }
        public int getSunCost()                        { return sunCost; }
        public Float getActionIntervalSeconds()        { return actionIntervalSeconds; }
        public Float getRechargeSeconds()              { return rechargeSeconds; }
        public int getRangeTiles()                     { return rangeTiles; }
        public int getPierceCount()                    { return pierceCount; }
        public float getEffectDurationBonusSeconds()   { return effectDurationBonusSeconds; }
        public List<String> getUnlockedFlags()         { return unlockedFlags; }
    }
}
