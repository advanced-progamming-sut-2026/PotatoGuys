package pvz.Models.Games.Levels.variants;

import java.util.List;

import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.data.GameMapDefinition;
import pvz.Models.User.MyPlant;

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
