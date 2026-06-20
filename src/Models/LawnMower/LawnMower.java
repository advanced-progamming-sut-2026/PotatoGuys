package Models.LawnMower;

import Models.DataTypes.Vector2;
import Models.Engine.TickAware;
import Models.Zombies.Zombie;

public class LawnMower implements TickAware{
    private Vector2 position;
    private Vector2 velocity;
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

    @Override
    public void onFirstTick() {

    }

    @Override
    public void onTick() {

    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }
}
