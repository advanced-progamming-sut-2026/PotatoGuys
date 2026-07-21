package pvz.models.games.map;

import pvz.models.Constants;
import pvz.models.games.map.tile.Tile;

public class GameMap {
    private int rows;
    private int columns;
    private Tile[][] map;

    public GameMap(int rows, int columns){
        this.rows=rows;
        this.columns=columns;
        map=new Tile[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                map[i][j]=new Tile();
            }
        }
    }

    public GameMap(){
        this(Constants.DEFAULT_ROWS, Constants.DEFAULT_COLS);
    }

    public Tile getTile(int col, int lane) {
        if (col < 0 || col >= columns || lane < 0 || lane >= rows) {
            throw new IndexOutOfBoundsException("Invalid column or lane index.");
        }
        return map[lane][col];
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
