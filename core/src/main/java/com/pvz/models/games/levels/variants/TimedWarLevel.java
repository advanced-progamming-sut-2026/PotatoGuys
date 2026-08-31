package com.pvz.models.games.levels.variants;

import java.util.List;

import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelType;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;

/**
 * A Timed War level: like a normal level (waves of zombies) but with a single
 * objective the player must complete — kill {@code targetKills} zombies within
 * any {@code windowSeconds}-long interval — before clearing every zombie in the
 * level.
 */
public class TimedWarLevel extends Level {
    private final List<Wave> waves;
    private final int targetKills;
    private final float windowSeconds;

    public TimedWarLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
            List<Wave> waves, int targetKills, float windowSeconds) {
        super(GameModeType.TIMEDWAR, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
        this.targetKills = targetKills;
        this.windowSeconds = windowSeconds;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public int getTargetKills() {
        return targetKills;
    }

    public float getWindowSeconds() {
        return windowSeconds;
    }
}
