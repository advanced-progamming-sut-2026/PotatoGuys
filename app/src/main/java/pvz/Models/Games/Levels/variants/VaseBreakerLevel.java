package pvz.models.games.levels.variants;

import java.util.List;

import pvz.models.entities.zombies.ZombieType;
import pvz.models.games.levels.Level;
import pvz.models.games.levels.LevelType;
import pvz.models.games.levels.data.VaseDefinition;
import pvz.models.games.map.data.GameMapDefinition;
import pvz.models.games.modes.GameModeType;
import pvz.models.user.MyPlant;

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