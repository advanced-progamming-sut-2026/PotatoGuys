package Models.LawnMower;

import Models.Engine.TickAware;
import Models.Zombies.Zombie;

public class LawnMower implements TickAware{
    private int row;
    private boolean available;
    private int killedZombiesCount;

    public LawnMower(int row) {
        this.row = row;
        this.available = true;
        this.killedZombiesCount = 0;
    }

    public void onZombieContact(Zombie zombie){

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

    public void onFirstTick(){}
    public void onTick(){}
}
