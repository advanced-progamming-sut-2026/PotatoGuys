package Models.MiniGames;

import Models.Seasons.Levels.Level;

public class VaseBreaker extends Level {
    private int rows;
    private int cols;
    private Vase[][] vases;

    public VaseBreaker(int rows, int cols){
        super();
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
