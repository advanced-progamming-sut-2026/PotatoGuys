package pvz.Models.Games.map;

import pvz.Models.Constants;

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
