package com.pvz.models.games.levels.variants;

import java.util.List;

import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelType;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;
import com.pvz.models.user.MyPlant;

public class ConveyorBeltLevel extends Level {
    private final List<Wave> waves;
    private final List<MyPlant> allowedPlantTypes;

    public ConveyorBeltLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
            List<Wave> waves, List<MyPlant> allowedPlantTypes) {
        super(GameModeType.CONVEYORBELT, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
        this.allowedPlantTypes = allowedPlantTypes;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    @Override
    public boolean hasPreGame() {
        return false;
    }

    public List<MyPlant> getAllowedPlantTypes() {
        return allowedPlantTypes;
    }
}
