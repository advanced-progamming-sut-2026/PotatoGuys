package com.pvz.models.entities.zombies.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pvz.models.Constants;
import com.pvz.utils.SaveManager;

/**
 * Singleton registry holding all {@link ZombiePropertySheet} and
 * {@link ArmorPropertySheet} definitions.
 *
 * <p>Data is loaded at startup from the data-driven {@code zombie_profiles.json}
 * resource via {@link SaveManager} (Gson) — the zombie-side counterpart of
 * {@link pvz.models.entities.plants.data.PlantRegistry}. Rebalancing or adding
 * a zombie means editing that JSON file only; this class never hard-codes any
 * zombie's stats.
 *
 * <p>Lookup is by <em>alias</em> string (the first element of the JSON
 * {@code "aliases"} array), e.g. {@code "ZombieMummyDefault"}.
 */
public final class ZombieRegistry {

    private static final ZombieRegistry INSTANCE = new ZombieRegistry();

    private final Map<String, ZombiePropertySheet> zombieSheets = new HashMap<>();
    private final Map<String, ArmorPropertySheet>  armorSheets  = new HashMap<>();
    private final Map<String, List<ScaledProp>> scalingPresets = new HashMap<>();

    private ZombieRegistry() {
        load();
    }

    public static ZombieRegistry getInstance() { return INSTANCE; }

    /** @return the sheet for the given alias, or {@code null} if not registered */
    public ZombiePropertySheet getSheet(String alias) { return zombieSheets.get(alias); }

    /** @return the armour sheet for the given alias, or {@code null} if not registered */
    public ArmorPropertySheet getArmorSheet(String alias) { return armorSheets.get(alias); }

    public int size() { return zombieSheets.size(); }

    // ── Loading ───────────────────────────────────────────────────────────────

    private void load() {
        RootDto root = SaveManager.getInstance().loadAbsolute(Constants.ZOMBIE_PROFILES_PATH, RootDto.class);
        if (root == null) {
            System.err.println("[ZombieRegistry] zombie_profiles.json could not be loaded; "
                    + "no zombies will be available.");
            return;
        }
        loadScalingPresets(root);
        loadArmors(root);
        loadZombies(root);
    }

    private void loadScalingPresets(RootDto root) {
        if (root.scalingPresets == null) return;
        for (Map.Entry<String, List<ScaledPropDto>> entry : root.scalingPresets.entrySet()) {
            List<ScaledProp> props = new ArrayList<>();
            for (ScaledPropDto dto : entry.getValue()) {
                props.add(new ScaledProp(dto.key, dto.formula, dto.arg1, dto.arg2));
            }
            scalingPresets.put(entry.getKey(), props);
        }
    }

    private void loadArmors(RootDto root) {
        if (root.armors == null) return;
        for (ArmorDto dto : root.armors) {
            armorSheets.put(dto.alias, new ArmorPropertySheet(
                    dto.alias, dto.type, dto.baseHealth,
                    dto.flags == null ? List.of() : dto.flags,
                    dto.layerThresholds == null ? new float[]{0.666f, 0.333f} : dto.layerThresholds));
        }
    }

    private void loadZombies(RootDto root) {
        if (root.zombies == null) return;
        for (ZombieDto dto : root.zombies) {
            try {
                zombieSheets.put(dto.alias, toSheet(dto));
            } catch (RuntimeException e) {
                System.err.println("[ZombieRegistry] Skipping malformed zombie '"
                        + dto.alias + "': " + e.getMessage());
            }
        }
    }

    private ZombiePropertySheet toSheet(ZombieDto dto) {
        List<ZombieStatEntry> stats = new ArrayList<>();
        if (dto.zombieStats != null) {
            for (ZombieStatDto s : dto.zombieStats) stats.add(new ZombieStatEntry(s.type, s.value));
        }

        return new ZombiePropertySheet.Builder(dto.alias, dto.objClass)
                .hitPoints(dto.hitPoints)
                .eatDps(dto.eatDps)
                .speed(dto.speed)
                .wavePointCost(dto.wavePointCost)
                .weight(dto.weight)
                .canSpawnPlantFood(dto.canSpawnPlantFood)
                .scaledProps(resolveScaling(dto.scaling))
                .armorAliases(dto.armorAliases == null ? List.of() : dto.armorAliases)
                .zombieStats(stats)
                .impType(dto.impType)
                .healthThresholdToThrowImp(dto.healthThresholdToThrowImp)
                .smashDamage(dto.smashDamage)
                .smashDuration(dto.smashDuration == 0 ? 2f : dto.smashDuration)
                .maxClaimedSunCurrency(dto.maxClaimedSunCurrency)
                .ammo(dto.ammo)
                .numberOfTombsToSpawn(dto.numberOfTombsToSpawn)
                .timeBetweenRaisings(dto.timeBetweenRaisings)
                .maxTorchReach(dto.maxTorchReach)
                .snowballsPerBarrage(dto.snowballsPerBarrage)
                .farAttackRange(dto.farAttackRange)
                .nearAttackRange(dto.nearAttackRange)
                .numberOfIceblocksToSpawnWith(dto.numberOfIceblocksToSpawnWith)
                .imp(dto.imp)
                .build();
    }

    private List<ScaledProp> resolveScaling(String scaling) {
        String key = scaling == null ? "standard" : scaling;
        return scalingPresets.getOrDefault(key, scalingPresets.getOrDefault("standard", List.of()));
    }

    // ── Gson wire-format DTOs (mirror zombie_profiles.json exactly) ───────────

    private static final class RootDto {
        Map<String, List<ScaledPropDto>> scalingPresets;
        List<ArmorDto> armors;
        List<ZombieDto> zombies;
    }

    private static final class ScaledPropDto {
        String key;
        String formula;
        float arg1;
        float arg2;
    }

    private static final class ArmorDto {
        String alias;
        String type;
        float baseHealth;
        List<String> flags;
        float[] layerThresholds;
    }

    private static final class ZombieStatDto {
        String type;
        String value;
    }

    private static final class ZombieDto {
        String alias;
        String objClass;
        String scaling;
        float hitPoints;
        float eatDps;
        float speed;
        int wavePointCost;
        int weight;
        boolean canSpawnPlantFood;
        List<String> armorAliases;
        List<ZombieStatDto> zombieStats;
        String impType;
        float healthThresholdToThrowImp;
        float smashDamage;
        float smashDuration;
        int maxClaimedSunCurrency;
        int ammo;
        int numberOfTombsToSpawn;
        float timeBetweenRaisings;
        float maxTorchReach;
        int snowballsPerBarrage;
        int farAttackRange;
        int nearAttackRange;
        int numberOfIceblocksToSpawnWith;
        boolean imp;
    }
}

