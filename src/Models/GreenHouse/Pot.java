package Models.GreenHouse;

import Models.Plants.Plant;

public class Pot {
    private int x;
    private int y;
    private boolean locked;
    private Plant plant;

    public Pot(int x, int y, boolean locked) {
        this.x = x;
        this.y = y;
        this.locked = locked;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public boolean isEmpty() {
        return plant == null;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Plant getPlant() {
        return plant;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        locked = false;
    }
}
