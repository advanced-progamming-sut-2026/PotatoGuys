package Models.MiniGames;


import Models.DataTypes.Vector2;
import Models.Engine.TickAware;

public class Brain implements TickAware{
    private Vector2 pos;
    private boolean hasEatten;

    public Vector2 getPos() {
        return pos;
    }
    public boolean isHasEatten() {
        return hasEatten;
    }
    public void onFirstTick(){}
    public void onTick(){}
}
