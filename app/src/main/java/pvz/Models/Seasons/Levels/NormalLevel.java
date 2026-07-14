package pvz.Models.Seasons.Levels;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Plant;

import java.util.List;

public class NormalLevel extends Level{
    List<Plant> plants;
    public NormalLevel(GameEngine engine, GameMap gameMap, int levelNumber, int initialSun, List<Wave> waves, List<Plant> plants) {
        super(engine, gameMap, levelNumber, LevelType.NORMAL, initialSun, waves);
        this.plants=plants;
    }
}
