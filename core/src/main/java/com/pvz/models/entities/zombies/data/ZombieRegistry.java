package com.pvz.models.entities.zombies.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.pvz.models.Constants;

import com.pvz.models.entities.zombies.config.ScaledPropConfig;
import com.pvz.models.entities.zombies.config.ZombieArmorConfig;
import com.pvz.models.entities.zombies.config.ZombieConfigRegistry;
import com.pvz.models.entities.zombies.config.ZombieJsonConfig;

/**
 * Singleton registry holding all {@link ZombiePropertySheet} and
 * {@link ArmorPropertySheet} definitions.
 *
 * <p>
 * Data is loaded at startup from the single data-driven
 * {@code zombie_actions.json} resource (which absorbed the old
 * {@code zombie_profiles.json}: stats, scaling presets and armour definitions)
 * via {@link ZombieConfigRegistry} — the zombie-side counterpart of
 * {@link com.pvz.models.entities.plants.config.PlantConfigRegistry}.
 * Rebalancing or adding
 * a zombie means editing that JSON file only; this class never hard-codes any
 * zombie's stats.
 *
 * <p>
 * Lookup is by <em>alias</em> string, e.g. {@code "ZombieMummyDefault"}.
 */
public final class ZombieRegistry {

    private static final ZombieRegistry INSTANCE = new ZombieRegistry();

    private final Map<String, ZombiePropertySheet> zombieSheets = new HashMap<>();
    private final Map<String, ArmorPropertySheet> armorSheets = new HashMap<>();
    private final Map<String, String[]> descriptions = new HashMap<>();

    private ZombieRegistry() {
        load();
        loadDescriptions();
    }

    public static ZombieRegistry getInstance() {
        return INSTANCE;
    }

    /** @return the sheet for the given alias, or {@code null} if not registered */
    public ZombiePropertySheet getSheet(String alias) {
        return zombieSheets.get(alias);
    }

    /**
     * @return the armour sheet for the given alias, or {@code null} if not
     *         registered
     */
    public ArmorPropertySheet getArmorSheet(String alias) {
        return armorSheets.get(alias);
    }

    public int size() {
        return zombieSheets.size();
    }

    /** @return {overallDisc, funDisc} or null if no descriptions for this alias */
    public String[] getDescriptions(String alias) {
        return descriptions.get(alias);
    }

    // ── Loading (from zombie_actions.json only) ───────────────────────────────

    private void load() {
        ZombieConfigRegistry cfgRegistry = ZombieConfigRegistry.getInstance();

        for (ZombieArmorConfig armor : cfgRegistry.getAllArmors()) {
            armorSheets.put(armor.alias, toArmorSheet(armor));
        }

        for (ZombieJsonConfig cfg : cfgRegistry.getAllConfigs()) {
            try {
                zombieSheets.put(cfg.alias, toSheet(cfg, cfgRegistry));
            } catch (RuntimeException e) {
                System.err.println("[ZombieRegistry] Skipping malformed zombie '"
                        + cfg.alias + "': " + e.getMessage());
            }
        }
    }

    private ArmorPropertySheet toArmorSheet(ZombieArmorConfig a) {
        return new ArmorPropertySheet(
                a.alias, a.type, a.baseHealth,
                a.flags == null ? List.of() : a.flags,
                a.layerThresholds == null ? new float[] { 0.666f, 0.333f } : a.layerThresholds);
    }

    private ZombiePropertySheet toSheet(ZombieJsonConfig cfg, ZombieConfigRegistry cfgRegistry) {
        return new ZombiePropertySheet.Builder(cfg.alias, cfg.objClass)
                .hitPoints(cfg.hitPoints)
                .eatDps(cfg.eatDps)
                .speed(cfg.speed)
                .wavePointCost(cfg.wavePointCost)
                .weight(cfg.weight)
                .canSpawnPlantFood(cfg.canSpawnPlantFood)
                .scaledProps(resolveScaling(cfg.scaling, cfgRegistry))
                .armorAliases(cfg.armorAliases == null ? List.of()
                        : java.util.Arrays.asList(cfg.armorAliases))
                .impType(cfg.impType)
                .healthThresholdToThrowImp(cfg.healthThresholdToThrowImp)
                .smashDamage(cfg.smashDamage)
                .smashDuration(cfg.smashDuration == 0 ? 2f : cfg.smashDuration)
                .maxClaimedSunCurrency(cfg.maxClaimedSunCurrency)
                .ammo(cfg.ammo)
                .numberOfTombsToSpawn(cfg.numberOfTombsToSpawn)
                .timeBetweenRaisings(cfg.timeBetweenRaisings)
                .maxTorchReach(cfg.maxTorchReach)
                .snowballsPerBarrage(cfg.snowballsPerBarrage)
                .farAttackRange(cfg.farAttackRange)
                .nearAttackRange(cfg.nearAttackRange)
                .numberOfIceblocksToSpawnWith(cfg.numberOfIceblocksToSpawnWith)
                .imp(cfg.imp)
                .overallDisc(cfg.overallDisc)
                .funDisc(cfg.funDisc)
                .animationConfig(cfg.animationConfig)
                .walkConfig(cfg.walkConfig)
                .eatConfig(cfg.eatConfig)
                .dieConfig(cfg.dieConfig)
                .skillConfig(cfg.skillConfig)
                .build();
    }

    private List<ScaledProp> resolveScaling(String scaling, ZombieConfigRegistry cfgRegistry) {
        String key = scaling == null ? "standard" : scaling;
        ScaledPropConfig[] preset = cfgRegistry.getScalingPreset(key);
        if (preset == null) {
            preset = cfgRegistry.getScalingPreset("standard");
        }
        if (preset == null)
            return List.of();
        List<ScaledProp> props = new ArrayList<>();
        for (ScaledPropConfig dto : preset) {
            props.add(new ScaledProp(dto.key, dto.formula, dto.arg1, dto.arg2));
        }
        return props;
    }

    private void loadDescriptions() {
        String path = Constants.ZOMBIE_ACTIONS_PATH.replace("zombie_actions.json", "zombie_descriptions.json");
        try {
            String content = Gdx.files.internal(path).readString();
            JsonValue root = new JsonReader().parse(content);
            for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                String alias = entry.name;
                String overall = entry.has("overallDisc") ? entry.getString("overallDisc") : "";
                String fun = entry.has("funDisc") ? entry.getString("funDisc") : "";
                descriptions.put(alias, new String[] { overall, fun });
            }
        } catch (Exception e) {
            System.err.println("[ZombieRegistry] Could not load zombie_descriptions.json: " + e.getMessage());
        }
    }
}
