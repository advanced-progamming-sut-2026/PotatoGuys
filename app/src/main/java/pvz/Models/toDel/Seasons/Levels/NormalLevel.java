package pvz.Models.toDel.Seasons.Levels;

import java.util.List;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.map.GameMap;

public class NormalLevel extends Level{
    private     List<Plant> plants;
    public NormalLevel(GameEngine engine, GameMap gameMap, int levelNumber, int initialSun, List<Wave> waves, List<Plant> plants) {
        super(engine, gameMap, levelNumber, LevelType.NORMAL, initialSun, waves);
        this.plants=plants;
    }

    public List<Plant> getPlants() {
        return plants;
    }
}
