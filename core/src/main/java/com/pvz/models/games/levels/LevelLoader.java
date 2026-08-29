package com.pvz.models.games.levels;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.pvz.models.games.levels.variants.*;
import com.pvz.utils.SaveManager;

public class LevelLoader {
    private static final String BASE_PATH = "core/src/main/java/com/resources/data/seasons/";

    public static Level loadLevel(String seasonName, int levelNumber) {
        String subDir = seasonName.equalsIgnoreCase("izombie") ? "izombie" : seasonName.toLowerCase();
        String path = BASE_PATH + subDir + "/level_" + levelNumber + ".json";

        JsonObject jsonObject = SaveManager.getInstance().loadAbsolute(path, JsonObject.class);
        String type = jsonObject.get("gameType").getAsString();
        Gson gson = new Gson();
        Level level;

        if (type != null) {
            level = switch (type) {
                case "NORMAL" -> gson.fromJson(jsonObject, NormalLevel.class);
                case "IZOMBIE" -> gson.fromJson(jsonObject, IZombieLevel.class);
                case "CONVEYORBELT" -> gson.fromJson(jsonObject, ConveyorBeltLevel.class);
                case "TIMEDWAR" -> gson.fromJson(jsonObject, TimedWarLevel.class);
                case "VASEBREAKER" -> gson.fromJson(jsonObject, VaseBreakerLevel.class);
                case "DEADLINE" -> gson.fromJson(jsonObject, DeadLineLevel.class);
                case "PLANTWHATYOUGET" -> gson.fromJson(jsonObject, PlantWhatYouGetLevel.class);
                case "BEGHOULED" -> gson.fromJson(jsonObject, BeghouledLevel.class);
                case "WALLNUTBOWLING" -> gson.fromJson(jsonObject, WallnutBowlingLevel.class);
                default -> throw new IllegalArgumentException("Unknown game mode type: " + type);
            };
        } else {
            return null;
        }

        level.setSeasonName(seasonName);
        return level;
    }
}
