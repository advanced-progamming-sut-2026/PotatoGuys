package pvz.Models.Games.Levels.variants;

import java.util.List;

import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.data.GameMapDefinition;
import pvz.Models.User.MyPlant;

public class IZombieLevel extends Level {
    private final List<MyPlant> basedPlants; 

    public IZombieLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun, 
                        List<MyPlant> basedPlants) {
        super(GameModeType.IZOMBIE, gameMap, levelNumber, type, initialSun);
        this.basedPlants = basedPlants;
    }
    
    public List<MyPlant> getBasedPlants() {
        return basedPlants;
    }

    @Override
    public boolean hasPreGame() {
        return false;
    }

}
