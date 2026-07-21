package pvz.models.entities.lawnmower;

import pvz.models.engine.TickAware;
import pvz.models.entities.zombies.Zombie;

public class LawnMower implements TickAware{
    private int y;
    private float x;
    private boolean available;
    private int killedZombiesCount;

    public LawnMower(int row) {
        this.y = row;
        this.available = true;
        this.killedZombiesCount = 0;
    }

    public void onZombieContact(Zombie zombie){
        
    }

    public boolean isAvailable() {
        return available;
    }

    public int getKilledZombiesCount() {
        return killedZombiesCount;

    }

    @Override
    public void enter() {

    }

    @Override
    public void update() {

    }

    @Override
    public void dispose() {

    }
}
