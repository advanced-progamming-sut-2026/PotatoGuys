package com.pvz.models.entities.zombies.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.ObjectMap;
import com.pvz.models.Constants;

/**
 * Singleton registry mapping every zombie {@code alias} to its
 * {@link ZombieJsonConfig} from {@code zombie_actions.json} — the zombie
 * counterpart of {@link com.pvz.models.entities.plants.config.PlantConfigRegistry}.
 *
 * <p>Also caches the armour definitions and scaling presets from the same file,
 * so {@code zombie_actions.json} is the single source of truth for zombies.
 * Loaded once at startup via {@link ZombieActionConfigLoader}. Unknown or
 * malformed entries are skipped with a warning so one bad record never blocks
 * the whole registry.
 */
public final class ZombieConfigRegistry {

    private static final ZombieConfigRegistry INSTANCE = new ZombieConfigRegistry();

    private final Map<String, ZombieJsonConfig> configs = new HashMap<>();
    private final Map<String, ZombieArmorConfig> armors = new HashMap<>();
    private final Map<String, ScaledPropConfig[]> scalingPresets = new HashMap<>();

    private ZombieConfigRegistry() {
        load();
    }

    public static ZombieConfigRegistry getInstance() {
        return INSTANCE;
    }

    public ZombieJsonConfig getConfig(String alias) {
        return alias == null ? null : configs.get(alias);
    }

    public ZombieArmorConfig getArmor(String alias) {
        return alias == null ? null : armors.get(alias);
    }

    public ScaledPropConfig[] getScalingPreset(String name) {
        return scalingPresets.get(name);
    }

    public List<ZombieJsonConfig> getAllConfigs() {
        return new ArrayList<>(configs.values());
    }

    public List<ZombieArmorConfig> getAllArmors() {
        return new ArrayList<>(armors.values());
    }

    private void load() {
        ZombieRootConfig root = ZombieActionConfigLoader.loadFromFile(Constants.ZOMBIE_ACTIONS_PATH);
        if (root == null) {
            System.err.println("[ZombieConfigRegistry] zombie_actions.json could not be loaded.");
            return;
        }
        if (root.scalingPresets != null) {
            for (ObjectMap.Entry<String, ScaledPropConfig[]> entry : root.scalingPresets.entries()) {
                scalingPresets.put(entry.key, entry.value);
            }
        }
        if (root.armors != null) {
            for (ZombieArmorConfig armor : root.armors) {
                if (armor == null || armor.alias == null) continue;
                armors.put(armor.alias, armor);
            }
        }
        if (root.zombies != null) {
            for (ZombieJsonConfig cfg : root.zombies) {
                if (cfg == null || cfg.alias == null) {
                    System.err.println("[ZombieConfigRegistry] Skipping entry with missing alias.");
                    continue;
                }
                configs.put(cfg.alias, cfg);
            }
        }
    }
}
