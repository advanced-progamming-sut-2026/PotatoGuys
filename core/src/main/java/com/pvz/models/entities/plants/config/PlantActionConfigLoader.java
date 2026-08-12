package com.pvz.models.entities.plants.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public final class PlantActionConfigLoader {

    private PlantActionConfigLoader() {
    }

    public static Json newJson() {
        Json json = new Json(OutputType.json);
        json.addClassTag("ShooterActionConfig", ShooterActionConfig.class);
        json.addClassTag("LobberActionConfig", LobberActionConfig.class);
        json.addClassTag("SunProducerActionConfig", SunProducerActionConfig.class);
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
