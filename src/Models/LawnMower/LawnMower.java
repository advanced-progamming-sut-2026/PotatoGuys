package Models.LawnMower;

public class LawnMower {
    private int row;
    private boolean available;
    private int killedZombiesCount;

    public LawnMower(int row) {
        this.row = row;
        this.available = true;
        this.killedZombiesCount = 0;
    }

    public int getRow() {
        return row;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getKilledZombiesCount() {
        return killedZombiesCount;
    }
}
