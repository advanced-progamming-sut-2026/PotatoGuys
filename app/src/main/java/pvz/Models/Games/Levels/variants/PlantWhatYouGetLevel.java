package pvz.models.games.levels.variants;

import java.util.ArrayList;
import java.util.List;

import pvz.models.entities.plants.data.PlantPropertySheet;
import pvz.models.entities.plants.data.PlantRegistry;
import pvz.models.entities.plants.enums.PlantCategory;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.games.levels.Level;
import pvz.models.games.levels.LevelType;
import pvz.models.games.levels.Wave;
import pvz.models.games.map.data.GameMapDefinition;
import pvz.models.games.modes.GameModeType;

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