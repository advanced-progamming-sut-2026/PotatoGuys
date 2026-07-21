package pvz.models.games.levels;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pvz.models.games.levels.variants.BeghouledLevel;
import pvz.models.games.levels.variants.ConveyorBeltLevel;
import pvz.models.games.levels.variants.DeadLineLevel;
import pvz.models.games.levels.variants.IZombieLevel;
import pvz.models.games.levels.variants.NormalLevel;
import pvz.models.games.levels.variants.PlantWhatYouGetLevel;
import pvz.models.games.levels.variants.TimedWarLevel;
import pvz.models.games.levels.variants.VasebreakerLevel;
import pvz.utils.SaveManager;

public class LevelLoader {
    private static final String BASE_PATH = "app/src/main/resources/data/seasons/";

    public static Level loadLevel(String seasonName, int levelNumber) {
        String path = BASE_PATH + seasonName.toLowerCase() + "/level_" + levelNumber + ".json";

        JsonObject jsonObject = SaveManager.getInstance().loadAbsolute(path, JsonObject.class);
        String type = jsonObject.get("gameType").getAsString();
        Gson gson = new Gson();
        Level level;

        if (type != null) {
            switch (type) {
                case "NORMAL":
                    level = gson.fromJson(jsonObject, NormalLevel.class);
                    break;

                case "IZOMBIE":
                    level = gson.fromJson(jsonObject, IZombieLevel.class);
                    break;

                case "CONVEYORBELT":
                    level = gson.fromJson(jsonObject, ConveyorBeltLevel.class);
                    break;

                case "TIMEDWAR":
                    level = gson.fromJson(jsonObject, TimedWarLevel.class);
                    break;

                case "VASEBREAKER":
                    level = gson.fromJson(jsonObject, VasebreakerLevel.class);
                    break;

                case "DEADLINE":
                    level = gson.fromJson(jsonObject, DeadLineLevel.class);
                    break;

                case "PLANTWHATYOUGET":
                    level = gson.fromJson(jsonObject, PlantWhatYouGetLevel.class);
                    break;

                case "BEGHOULED":
                    level = gson.fromJson(jsonObject, BeghouledLevel.class);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown game mode type: " + type);
            }
        } else {
            return null;
        }

        level.setSeasonName(seasonName);
        return level;
    }
}
