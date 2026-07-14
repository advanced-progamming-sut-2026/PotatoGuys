package pvz.Controller.Game;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.GameSession;
import pvz.Models.Seasons.Season;
import pvz.View.Game.GameMenu;
import pvz.View.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public abstract class PreGameController {

    Season season;
    int level;

    public PreGameController(Season season,int level){
        this.season=season;
        this.level=level;
    }

    public abstract Result startGame(Matcher matcher);
}
