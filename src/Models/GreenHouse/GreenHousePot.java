package Models.GreenHouse;

public class GreenHousePot {

    private int x;
    private int y;
    private boolean locked;
    private GreenHousePlant greenHousePlant;

    public GreenHousePot(int x, int y, boolean locked) {
        this.x = x;
        this.y = y;
        this.locked = locked;
    }

    public boolean isEmpty() {
        return getGreenHousePlant() == null;
    }

    public void setPlant(GreenHousePlant greenHousePlant) {
        this.greenHousePlant = greenHousePlant;
    }

    public GreenHousePlant getPlant() {
        return getGreenHousePlant();
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

    public GreenHousePlant getGreenHousePlant() {
        return greenHousePlant;
    }
}
