package pvz.models.games.levels.variants;

import java.util.List;

import pvz.models.entities.zombies.ZombieType;
import pvz.models.games.levels.Level;
import pvz.models.games.levels.LevelType;
import pvz.models.games.map.data.GameMapDefinition;
import pvz.models.games.modes.GameModeType;
import pvz.models.user.MyPlant;

public class IZombieLevel extends Level {
    private List<MyPlant> basedPlants; 
    private List<ZombieType> basedZombies;
    private int redLineColumn;

    public IZombieLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun, 
                        List<MyPlant> basedPlants, List<ZombieType> basedZombies) {
        super(GameModeType.IZOMBIE, gameMap, levelNumber, type, initialSun);
        this.basedPlants = basedPlants;
        this.basedZombies = basedZombies;
        this.redLineColumn = 4; // Default value, you can change it as needed
    }
    
    public List<MyPlant> getBasedPlants() {
        return basedPlants;
    }

    public List<ZombieType> getBasedZombies() {
        return basedZombies;
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    @Override
    public boolean hasPreGame() {
        return false;
    }

}
