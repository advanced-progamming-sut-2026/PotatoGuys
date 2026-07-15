package pvz.Models.toDel.Seasons.Levels;

import java.util.List;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.GameMap;

public class NormalLevel extends Level{
    private List<Plant> plants;
    public NormalLevel(GameMap gameMap, int levelNumber, int initialSun, List<Plant> plants) {
        super(GameModeType.NORMAL, gameMap, levelNumber, LevelType.NORMAL, initialSun);
        this.plants=plants;
    }

    public List<Plant> getPlants() {
        return plants;
    }
}
