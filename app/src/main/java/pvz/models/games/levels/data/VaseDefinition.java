package pvz.models.games.levels.data;

public class VaseDefinition {
    private int lane;
    private int col;
    private VaseType vaseType;

    public VaseDefinition() {}

    public VaseDefinition(int lane, int col, VaseType vaseType) {
        this.lane = lane;
        this.col = col;
        this.vaseType = vaseType;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public VaseType getVaseType() {
        return vaseType;
    }

    public void setVaseType(VaseType vaseType) {
        this.vaseType = vaseType;
    }
}