package pvz.Models.MiniGames;


import pvz.Models.DataTypes.Vector2;
import pvz.Models.Engine.TickAware;

public class Brain implements TickAware{
    private Vector2 pos;
    private boolean hasEatten;

    public Vector2 getPos() {
        return pos;
    }
    public boolean isHasEatten() {
        return hasEatten;
    }
    public void enter(){}
    public void update(){}

    @Override
    public void dispose() {

    }
}
