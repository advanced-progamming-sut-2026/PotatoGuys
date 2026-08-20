package com.pvz.models.games.levels;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.levels.data.EffectDefinition;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;

public abstract class Level {
    protected GameModeType gameType;
    protected GameMapDefinition gameMap;
    protected LevelType levelType;
    protected int initialSun;
    protected int levelNumber;
    protected String seasonName;
    protected List<EffectDefinition> effects;
    protected List<String> objectives;
    protected String musicPath;

    protected Level() {
        this.effects = new ArrayList<>();
        this.objectives = new ArrayList<>();
    }

    public Level(GameModeType gameMode, GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun) {
        this.gameType = gameMode;
        this.gameMap = gameMap;
        this.levelNumber = levelNumber;
        this.levelType = type;
        this.initialSun = initialSun;
        this.seasonName = "";
        this.effects = new java.util.ArrayList<>();
    }

    public List<EffectDefinition> getEffects() {
        return effects;
    }

    public void setEffects(List<EffectDefinition> effects) {
        this.effects = effects;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public boolean hasPreGame() {
        return true;
    }

    public boolean isPlantAllowed(PlantType type) {
        return true;
    }

    public List<PlantType> getForcedPlants() {
        return new ArrayList<>();
    }

    public GameMapDefinition getGameMapDefinition() {
        return gameMap;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public LevelType getType() {
        return levelType;
    }

    public int getInitialSun() {
        return initialSun;
    }

    public void setGameMap(GameMapDefinition gameMap) {
        this.gameMap = gameMap;
    }

    public GameModeType getGameMode() {
        return gameType;
    }

    public void setGameMode(GameModeType gameMode) {
        this.gameType = gameMode;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public List<String> getObjectives() {
        return objectives != null ? objectives : new ArrayList<>();
    }
}
