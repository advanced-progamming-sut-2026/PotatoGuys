package Models.Sun;

import Models.DataTypes.Vector2;
import Models.Engine.TickAware;

public class Sun implements TickAware{
    private SunType type;
    private Vector2 position;
    private Vector2 velocity;
    private int timeRemainingToDespawn;

    public SunType getType() {
        return type;
    }
    public Vector2 getPosition() {
        return position;
    }
    public Vector2 getVelocity() {
        return velocity;
    }
    public int getTimeRemainingToDespawn() {
        return timeRemainingToDespawn;
    }
    public void onFirstTick(){}
    public void onTick(){}
}
