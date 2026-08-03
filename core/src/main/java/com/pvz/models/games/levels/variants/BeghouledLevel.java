package com.pvz.models.games.levels.variants;

import java.util.List;

import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelType;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;

public class BeghouledLevel extends Level {
    private final List<Wave> waves;

    public BeghouledLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
            List<Wave> waves) {
        super(GameModeType.NORMAL, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
    }

    public List<Wave> getWaves() {
        return waves;
    }
}
