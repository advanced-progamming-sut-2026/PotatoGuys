package Models.MiniGames;

import Models.Seasons.Levels.Tile;

import java.util.ArrayList;
import java.util.logging.Level;


public class WallnutBowling extends Level{
    private int redLine;

    protected WallnutBowling(String name, int value) {
        super(name, value);
    }

    public int getRedLine() {
        return redLine;
    }
    private boolean plantWalnut(Tile tile){
        return true;
    }
    private boolean canPlantAt(Tile tile){
        return true;
    }
    private boolean setupBoard(){
        return true;
    }

    public void onFirstTick(){}
    public void onTick(){}
}
