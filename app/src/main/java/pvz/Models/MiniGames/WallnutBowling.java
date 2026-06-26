package pvz.Models.MiniGames;

import java.util.logging.Level;

import pvz.Models.Seasons.Levels.Tile;


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
