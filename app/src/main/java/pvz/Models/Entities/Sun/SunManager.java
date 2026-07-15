package pvz.Models.Entities.Sun;

import pvz.Models.Engine.TickAware;
import pvz.Models.Games.GameContext;
import java.util.Random;

public class SunManager implements TickAware {
    private GameContext gameContext;
    private int ticksSinceLastDrop;
    private int totalTicks;
    private Random random = new Random();

    public SunManager(GameContext gameContext) {
        this.gameContext = gameContext;
        this.ticksSinceLastDrop = 0;
        this.totalTicks = 0;
    }

    @Override
    public void enter() {}

    @Override
    public void update() {
        totalTicks++;
        ticksSinceLastDrop++;
        
        double timeInSeconds = totalTicks / 10.0;
        double x_seconds = Math.min(6 + 0.05 * timeInSeconds, 12);
        int x_ticks = (int)(x_seconds * 10);
        
        if (ticksSinceLastDrop >= x_ticks) {
            spawnSun();
            ticksSinceLastDrop = 0;
        }
    }
    
    private void spawnSun() {
        int col = random.nextInt(gameContext.getMap().getColumns());
        int lane = random.nextInt(gameContext.getMap().getRows());
        
        SunType type = SunType.NORMAL;
        double rand = random.nextDouble();
        if (rand < 0.05) type = SunType.RADIOACTIVE;
        else if (rand < 0.10) type = SunType.SPECIAL;
        
        String typeName = (type == SunType.RADIOACTIVE) ? "radioactive" : 
                          (type == SunType.SPECIAL) ? "special" : "regular";

        gameContext.log("New " + typeName + " sun is dropping at position (" + col + ", " + lane + ")");
        
        Sun sun = new Sun(type, col, lane, 25);
        gameContext.getEngine().register(sun);
    }
    
    @Override
    public void dispose() {}
}
