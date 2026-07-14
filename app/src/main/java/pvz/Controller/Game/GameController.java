package pvz.Controller.Game;

import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Seasons.Levels.Level;
import pvz.View.Result;

public abstract class GameController {
    Level level;
    public GameController(Level level){
        this.level=level;
    }
    public Result advanceTime(Matcher matcher) {
        int ticks = Integer.parseInt(matcher.group("ticks"));
        level.getEngine().advanceTime(ticks);
        return new Result("Time advanced by " + ticks + " ticks.");
    }
    public Result releaseNuke(Matcher matcher) { return null; }
    public Result cheatCooldown(Matcher matcher) { return null; }
    public Result feedPlant(Matcher matcher) { return null; }
    public Result cheatPlantFood(Matcher matcher) { return null; }
    public Result showMap(Matcher matcher) { return null; }
    public Result showPlantsStatus(Matcher matcher) { return null; }
    public Result showTileStatus(Matcher matcher) { return null; }
    public Result zombiesInfo(Matcher matcher) { return null; }
    public Result cheatSpawnZombie(Matcher matcher) { return null; }
}
