package com.pvz.models.entities.plants.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.pvz.models.entities.plants.config.explosive.ExplosiveConfig;
import com.pvz.models.entities.plants.config.explosive.SquashConfig;
import com.pvz.models.entities.plants.config.explosive.TangleKelpConfig;

public final class PlantActionConfigLoader {

    private PlantActionConfigLoader() {
    }

    public static Json newJson() {
        Json json = new Json(OutputType.json);
        json.addClassTag("ShooterActionConfig", ShooterActionConfig.class);
        json.addClassTag("StackedShooterConfig", StackedShooterConfig.class);
        json.addClassTag("ChargingShooterConfig", ChargingShooterConfig.class);
        json.addClassTag("ClipProgressionShooterConfig", ClipProgressionShooterConfig.class);
        json.addClassTag("MultiStageShooterConfig", MultiStageShooterConfig.class);
        json.addClassTag("PassiveConfig", PassiveConfig.class);
        json.addClassTag("GrowthMeleeConfig", GrowthMeleeConfig.class);
        json.addClassTag("LobberActionConfig", LobberActionConfig.class);
        json.addClassTag("SunProducerActionConfig", SunProducerActionConfig.class);
        json.addClassTag("GrowthSunProducerConfig", GrowthSunProducerConfig.class);
        json.addClassTag("BonkChoyConfig", BonkChoyConfig.class);
        json.addClassTag("PhatBeetConfig", PhatBeetConfig.class);
        json.addClassTag("ChomperConfig", ChomperConfig.class);
        json.addClassTag("WasabiWhipConfig", WasabiWhipConfig.class);
        json.addClassTag("KiwiBeastConfig", KiwiBeastConfig.class);
        json.addClassTag("SplitPeaConfig", SplitPeaConfig.class);
        json.addClassTag("GraveBusterConfig", GraveBusterConfig.class);
        json.addClassTag("ExplosiveConfig", ExplosiveConfig.class);
        json.addClassTag("SquashConfig", SquashConfig.class);
        json.addClassTag("TangleKelpConfig", TangleKelpConfig.class);
        json.addClassTag("NutConfig", NutConfig.class);
        json.addClassTag("SunBeanConfig", SunBeanConfig.class);
        json.addClassTag("WallNutFeedConfig", WallNutFeedConfig.class);
        json.addClassTag("MintActionConfig", MintActionConfig.class);
        return json;
    }

    public static PlantJsonConfig[] loadFromFile(String absolutePath) {
        try {
            String content = Files.readString(Paths.get(absolutePath));
            return newJson().fromJson(PlantJsonConfig[].class, content);
        } catch (IOException e) {
            System.err.println("[PlantActionConfigLoader] Could not read " + absolutePath + ": " + e.getMessage());
            return new PlantJsonConfig[0];
        }
    }
}
