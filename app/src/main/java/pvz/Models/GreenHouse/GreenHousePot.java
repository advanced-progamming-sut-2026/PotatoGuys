package pvz.Models.GreenHouse;

public class GreenHousePot {

    private final int x;
    private final int y;
    private boolean locked;
    private GreenHousePlant greenHousePlant;

    public GreenHousePot(int x, int y, boolean locked) {
        this.x = x;
        this.y = y;
        this.locked = locked;
    }

    public boolean isEmpty() {
        return greenHousePlant == null;
    }

    public void setPlant(GreenHousePlant greenHousePlant) {
        this.greenHousePlant = greenHousePlant;
    }

    public GreenHousePlant getPlant() {
        return greenHousePlant;
    }

    public void clearPlant() {
        greenHousePlant = null;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        locked = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
