package com.pvz.models.games.levels.variants;

import java.util.List;

import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelType;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;

/**
 * Level variant for the Scored (mini-point) game mode. It uses the exact same
 * wave/lawn-defense structure as the normal mode, but the mode that consumes it
 * awards "miopoints" instead of a simple win/loss.
 */
public class ScoredLevel extends Level {
    private final List<Wave> waves;

    public ScoredLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(GameModeType.SCORED, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
    }

    public List<Wave> getWaves() {
        return waves;
    }
}
