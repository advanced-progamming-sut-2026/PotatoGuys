package com.pvz.models.games.levels.variants;

import java.util.List;

import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelType;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;
import com.pvz.models.user.MyPlant;

public class VaseBreakerLevel extends Level {
    private List<MyPlant> basedPlants;
    private List<ZombieType> basedZombies;
    private int cols;
    private int xOffset;
    private int greenPots;
    private int gargantuarPots;

    public VaseBreakerLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
            List<MyPlant> basedPlants, List<ZombieType> basedZombies, int cols, int lanes, int xOffset, int greenPots,
            int gargantuarPots) {
        super(GameModeType.VASEBREAKER, gameMap, levelNumber, type, initialSun);
        this.basedPlants = basedPlants;
        this.basedZombies = basedZombies;
        this.cols = cols;
        this.xOffset = xOffset;
        this.greenPots = greenPots;
        this.gargantuarPots = gargantuarPots;
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

    @Override
    public boolean hasPreGame() {
        return false;
    }

    public int getCols() {
        return cols;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getGreenPots() {
        return greenPots;
    }

    public int getGargantuarPots() {
        return gargantuarPots;
    }
}
