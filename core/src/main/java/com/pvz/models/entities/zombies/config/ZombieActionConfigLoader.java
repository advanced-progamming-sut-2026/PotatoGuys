package com.pvz.models.entities.zombies.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

/**
 * Loads {@code zombie_actions.json} using LibGDX's {@link Json} — the zombie
 * counterpart of
 * {@link com.pvz.models.entities.plants.config.PlantActionConfigLoader}.
 *
 * <p>
 * Every concrete {@link ZombieActionConfig}/{@link ZombieSkillConfig} subclass
 * is registered with a class tag so the polymorphic {@code walkConfig}/
 * {@code eatConfig}/…/{@code skillConfig} fields deserialize from the JSON
 * {@code "class"} key. The root object holds the scaling presets, the armour
 * definitions and the zombie entries (stats + animations + skills).
 */
public final class ZombieActionConfigLoader {

    private ZombieActionConfigLoader() {
    }

    public static Json newJson() {
        Json json = new Json(OutputType.json);
        // FSM state configs
        json.addClassTag("ZombieWalkConfig", ZombieWalkConfig.class);
        json.addClassTag("ZombieEatConfig", ZombieEatConfig.class);
        json.addClassTag("ZombieDieConfig", ZombieDieConfig.class);
        // Skill configs
        json.addClassTag("ExplorerTorchSkillConfig", ExplorerTorchSkillConfig.class);
        json.addClassTag("GargantuarSkillConfig", GargantuarSkillConfig.class);
        json.addClassTag("RaStealSunSkillConfig", RaStealSunSkillConfig.class);
        json.addClassTag("TombRaiserSkillConfig", TombRaiserSkillConfig.class);
        json.addClassTag("WizardZapSkillConfig", WizardZapSkillConfig.class);
        json.addClassTag("HunterSnowballSkillConfig", HunterSnowballSkillConfig.class);
        json.addClassTag("OctopusSkillConfig", OctopusSkillConfig.class);
        // scalingPresets holds ObjectMap<String, ScaledPropConfig[]>
        json.setElementType(ZombieRootConfig.class, "scalingPresets", ScaledPropConfig[].class);
        return json;
    }

    public static ZombieRootConfig loadFromFile(String internalPath) {
        try {
            String content = Gdx.files.internal(internalPath).readString();
            return newJson().fromJson(ZombieRootConfig.class, content);
        } catch (Exception e) {
            System.err.println("[ZombieActionConfigLoader] Could not read " + internalPath + ": " + e.getMessage());
            return null;
        }
    }
}
