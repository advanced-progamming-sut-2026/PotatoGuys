package pvz.Models.Games.Levels;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pvz.Models.Games.Levels.Data.LevelDefinition;
import pvz.Models.Games.Levels.Data.NormalLevelDefinition;
import pvz.Utils.SaveManager;

public class LevelLoader {
    private static final String BASE_PATH = "app/src/main/resources/data/seasons/";

    public static LevelDefinition loadLevel(String seasonName, int levelNumber) {
        String path = BASE_PATH + seasonName.toLowerCase() + "/level_" + levelNumber + ".json";

        JsonObject jsonObject = SaveManager.getInstance().loadAbsolute(path, JsonObject.class);
        String type = jsonObject.get("type").getAsString();
        Gson gson = new Gson();

        if ("NORMAL".equals(type)) {
            return gson.fromJson(jsonObject, NormalLevelDefinition.class);
        }
        // Add other types here
        return null;
    }
}
