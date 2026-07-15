package pvz.Models.MiniGames;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Seasons.Levels.GameMap;
import pvz.Models.Seasons.Levels.Level;
import pvz.Models.Seasons.Levels.LevelType;
import pvz.Models.Seasons.Levels.Wave;

import java.util.List;

public class VaseBreaker extends Level {
    private int rows;
    private int cols;
    private Vase[][] vases;

    public VaseBreaker(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(engine, gameMap, levelNumber, type, initialSun, waves);
    }


    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public Vase[][] getVases() {
        return vases;
    }

    public void setVases(Vase[][] vases) {
        this.vases = vases;
    }
}
