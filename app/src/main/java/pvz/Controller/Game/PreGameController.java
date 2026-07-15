package pvz.Controller.Game;

import java.util.regex.Matcher;

import pvz.Models.toDel.Seasons.Season;
import pvz.View.Result;

public abstract class PreGameController {

    Season season;
    int level;

    public PreGameController(Season season,int level){
        this.season=season;
        this.level=level;
    }

    public abstract Result startGame(Matcher matcher);
}
