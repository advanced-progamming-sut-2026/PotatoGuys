package pvz.Controller;

import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.Entities.Plants.Plant;
import pvz.View.Result;

public class GameController {
    List<Plant> plants;
    public GameController(List<Plant> plants){
        this.plants=plants;
    }
    public Result advanceTime(Matcher matcher) { return null; }
    public Result collectSun(Matcher matcher) { return null; }
    public Result showSun(Matcher matcher) { return null; }
    public Result cheatSun(Matcher matcher) { return null; }
    public Result releaseNuke(Matcher matcher) { return null; }
    public Result plant(Matcher matcher) { return null; }
    public Result cheatCooldown(Matcher matcher) { return null; }
    public Result pluckPlant(Matcher matcher) { return null; }
    public Result feedPlant(Matcher matcher) { return null; }
    public Result cheatPlantFood(Matcher matcher) { return null; }
    public Result showMap(Matcher matcher) { return null; }
    public Result showPlantsStatus(Matcher matcher) { return null; }
    public Result showTileStatus(Matcher matcher) { return null; }
    public Result zombiesInfo(Matcher matcher) { return null; }
    public Result cheatSpawnZombie(Matcher matcher) { return null; }
}
