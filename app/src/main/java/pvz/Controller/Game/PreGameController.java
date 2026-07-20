package pvz.Controller.Game;

import java.util.regex.Matcher;

import pvz.Models.Games.Levels.Level;
import pvz.View.Result;

public abstract class PreGameController {

    protected Level level;

    public PreGameController(Level level){
        this.level = level;
    }

    public abstract Result startGame(Matcher matcher);
}
