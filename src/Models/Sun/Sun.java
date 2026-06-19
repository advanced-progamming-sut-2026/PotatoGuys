package Models.Sun;

import Models.DataTypes.Vector2;
import Models.Engine.GameEngine;
import Models.Engine.TickAware;
import Models.Seasons.Levels.Level;

public class Sun implements TickAware{
    private SunType type;
    private Vector2 position;
    private Vector2 targetPosition;
    private Vector2 velocity;
    private Level level;
    private float timeRemainingToDespawn;

    public SunType getType() {
        return type;
    }
    public Vector2 getPosition() {
        return position;
    }
    public Vector2 getVelocity() {
        return velocity;
    }
    public float getTimeRemainingToDespawn() {
        return timeRemainingToDespawn;
    }
    public void onFirstTick(){}
    public void onTick(){
        if (Vector2.calculateDistance(position,targetPosition)> velocity.magnitude()*1.2) {
            position.x += velocity.x;
            position.y += velocity.y;
        }
        timeRemainingToDespawn -= 0.1f;

        //despawn sun
        if (timeRemainingToDespawn<=0){
            dispose();
        }
    }

    public void dispose(){
        level.getEngine().getToRemove().add(this);
    }
}
