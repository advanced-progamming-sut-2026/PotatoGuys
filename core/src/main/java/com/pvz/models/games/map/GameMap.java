package com.pvz.models.games.map;

import com.pvz.models.Constants;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.games.map.tile.Tile;

public class GameMap {
    public static final float TILE_WIDTH = 82f;
    public static final float TILE_HEIGHT = 96f;
    public static final float START_X = 255f;
    public static final float TOP_LANE_Y = 469f;
    private int lanes;
    private int cols;
    private Tile[][] map;

    public GameMap(int lanes, int columns){
        this.lanes=lanes;
        this.cols=columns;
        map=new Tile[lanes][columns];
        for (int i = 0; i < lanes; i++) {
            for (int j = 0; j < columns; j++) {
                float x = START_X + (j * TILE_WIDTH);
                float y = TOP_LANE_Y - (i * TILE_HEIGHT);
                map[i][j]=new Tile(j, i, x, y, TILE_WIDTH, TILE_HEIGHT);
                GameEngine.getInstance().getToAdd().add(map[i][j]);
            }
        }
    }

    public GameMap(){
        this(Constants.DEFAULT_ROWS, Constants.DEFAULT_COLS);
    }

    public Tile getTileAt(int col, int lane) {
        if (col < 0 || col >= cols || lane < 0 || lane >= lanes) {
            throw new IndexOutOfBoundsException("Invalid column or lane index.");
        }
        return map[lane][col];
    }

    public Tile getTileAt(float worldX, float worldY){
        // برای پرفورمنس بهتر به جای حلقه زدن، با یک محاسبه ساده ریاضی پیداش می‌کنیم
        int col = (int) Math.floor((worldX - START_X) / TILE_WIDTH);
        int lane = (int) Math.floor((TOP_LANE_Y + TILE_HEIGHT - worldY) / TILE_HEIGHT);

        // بررسی اینکه آیا خارج از محدوده کلیک شده یا نه
        if (col >= 0 && col < cols && lane >= 0 && lane < lanes) {
            return map[lane][col];
        }
        return null;
    }

    public int getLanes() {
        return lanes;
    }

    /** Alias for {@link #getLanes()}, matching the row/column terminology used elsewhere. */
    public int getRows() {
        return lanes;
    }

    public int getColumns() {
        return cols;
    }

    public Tile[][] getMap() {
        return map;
    }
}
