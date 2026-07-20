package pvz.Models.Games.Levels.variants;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Entities.Plants.Enums.PlantCategory;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.data.GameMapDefinition;

public class PlantWhatYouGetLevel extends Level {
    private final List<Wave> waves;

    public PlantWhatYouGetLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(GameModeType.PLANTWHATYOUGET, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    @Override
    public boolean isPlantAllowed(PlantType type) {
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(type);
        if(sheet.getCategory() == PlantCategory.SUN_PRODUCER){
            return false;
        }
        return true;
    }

    @Override
    public List<PlantType> getForcedPlants() {
        return new ArrayList<>();
    }
}