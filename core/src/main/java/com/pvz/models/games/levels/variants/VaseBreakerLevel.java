package com.pvz.models.games.levels.variants;

import java.util.List;

import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelType;
import com.pvz.models.games.levels.data.VaseDefinition;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;
import com.pvz.models.user.MyPlant;

public class VaseBreakerLevel extends Level {
    private List<MyPlant> basedPlants;
    private List<ZombieType> basedZombies;
    private List<VaseDefinition> vases;

    public VaseBreakerLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
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
