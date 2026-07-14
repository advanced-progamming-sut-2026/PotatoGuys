package pvz.Controller.Game;

import pvz.Models.Seasons.Levels.Level;
import pvz.Models.Seasons.Levels.NormalLevel;
import pvz.View.Result;

import java.util.regex.Matcher;

public class NormalGameController extends GameController{
    public NormalGameController(NormalLevel level) {
        super(level);
    }

    public Result collectSun(Matcher matcher) { return null; }
    public Result showSun(Matcher matcher) { return null; }
    public Result cheatSun(Matcher matcher) { return null; }
    public Result plant(Matcher matcher) { return null; }
    public Result pluckPlant(Matcher matcher) { return null; }
}
