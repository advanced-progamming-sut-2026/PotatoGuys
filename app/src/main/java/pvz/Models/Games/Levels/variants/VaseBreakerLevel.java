package pvz.Models.Games.Levels.variants;

import java.util.List;

import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Levels.Data.VaseDefinition;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.data.GameMapDefinition;
import pvz.Models.User.MyPlant;

public class VasebreakerLevel extends Level {
    private List<MyPlant> basedPlants;
    private List<ZombieType> basedZombies;
    private List<VaseDefinition> vases;

    public VasebreakerLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
                            List<MyPlant> basedPlants, List<ZombieType> basedZombies,
                            List<VaseDefinition> vases) {
        super(GameModeType.VASEBREAKER, gameMap, levelNumber, type, initialSun);
        this.basedPlants = basedPlants;
        this.basedZombies = basedZombies;
        this.vases = vases;
    }

    public List<MyPlant> getBasedPlants() {
        return basedPlants;
    }

    public void setBasedPlants(List<MyPlant> basedPlants) {
        this.basedPlants = basedPlants;
    }

    public List<ZombieType> getBasedZombies() {
        return basedZombies;
    }

    public void setBasedZombies(List<ZombieType> basedZombies) {
        this.basedZombies = basedZombies;
    }

    public List<VaseDefinition> getVases() {
        return vases;
    }

    public void setVases(List<VaseDefinition> vases) {
        this.vases = vases;
    }

    @Override
    public boolean hasPreGame() {
        return false;
    }
}