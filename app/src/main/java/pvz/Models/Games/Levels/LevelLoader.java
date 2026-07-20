package pvz.Models.Games.Levels;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pvz.Models.Games.Levels.variants.BeghouledLevel;
import pvz.Models.Games.Levels.variants.ConveyorBeltLevel;
import pvz.Models.Games.Levels.variants.DeadLineLevel;
import pvz.Models.Games.Levels.variants.IZombieLevel;
import pvz.Models.Games.Levels.variants.NormalLevel;
import pvz.Models.Games.Levels.variants.PlantWhatYouGetLevel;
import pvz.Models.Games.Levels.variants.TimedWarLevel;
import pvz.Models.Games.Levels.variants.VaseBreakerLevel;
import pvz.Utils.SaveManager;

public class LevelLoader {
    private static final String BASE_PATH = "app/src/main/resources/data/seasons/";

    public static Level loadLevel(String seasonName, int levelNumber) {
        String path = BASE_PATH + seasonName.toLowerCase() + "/level_" + levelNumber + ".json";

        JsonObject jsonObject = SaveManager.getInstance().loadAbsolute(path, JsonObject.class);
        String type = jsonObject.get("gameType").getAsString();
        Gson gson = new Gson();

        if (type != null) {
            switch (type) {
                case "NORMAL":
                    return gson.fromJson(jsonObject, NormalLevel.class);

                case "IZOMBIE":
                    return gson.fromJson(jsonObject, IZombieLevel.class);

                case "CONVEYORBELT":
                    return gson.fromJson(jsonObject, ConveyorBeltLevel.class);

                case "TIMEDWAR":
                    return gson.fromJson(jsonObject, TimedWarLevel.class);

                case "VASEBREAKER":
                    return gson.fromJson(jsonObject, VaseBreakerLevel.class);

                case "DEADLINE":
                    return gson.fromJson(jsonObject, DeadLineLevel.class);

                case "PLANTWHATYOUGET":
                    return gson.fromJson(jsonObject, PlantWhatYouGetLevel.class);

                case "BEGHOULED":
                    return gson.fromJson(jsonObject, BeghouledLevel.class);

                default:
                    throw new IllegalArgumentException("Unknown game mode type: " + type);
            }
        }

        return null;
    }
}
