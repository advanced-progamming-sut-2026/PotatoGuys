package Models.Engine;

import Models.Seasons.Levels.Tile;

public class GameMap {
    private int rows;
    private int columns;
    private Tile[][] map;

    public GameMap(int rows, int columns){
        this.rows=rows;
        this.columns=columns;
        map=new Tile[rows][columns];
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Tile[][] getMap() {
        return map;
    }
}
