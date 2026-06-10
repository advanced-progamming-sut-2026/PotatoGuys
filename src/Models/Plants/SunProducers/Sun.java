package Models.Plants.SunProducers;

import Models.DataTypes.Vector2;
import Models.Engine.TickAware;

public class Sun implements TickAware{
    private SunType type;
    private Vector2 position;
    private Vector2 velocity;
    private int timeRemainingToDespawn;
}
