package com.pvz.models.entities.sun;

import java.util.Random;

import com.pvz.models.engine.TickAware;
import com.pvz.models.games.GameContext;

public class SunManager implements TickAware {
    private final GameContext gameContext;
    private int ticksSinceLastDrop;
    private int totalTicks;
    private final Random random = new Random();

    public SunManager(GameContext gameContext) {
        this.gameContext = gameContext;
        this.ticksSinceLastDrop = 0;
        this.totalTicks = 0;
    }

    @Override
    public void enter() {
    }

    @Override
    public void update() {
        totalTicks++;
        ticksSinceLastDrop++;

        if (!gameContext.getMode().supportsFallingSuns()) {
            return;
        }
        if ("dark ages".equalsIgnoreCase(gameContext.getSeasonName())) {
            return;
        }

        double timeInSeconds = totalTicks / 10.0;
        double xSeconds = Math.min(6 + 0.05 * timeInSeconds, 12);
        int xTicks = (int) (xSeconds * 10);

        if (ticksSinceLastDrop >= xTicks) {
            spawnSun();
            ticksSinceLastDrop = 0;
        }
    }

    private void spawnSun() {
        int col = random.nextInt(gameContext.getMap().getColumns());
        int lane = random.nextInt(gameContext.getMap().getLanes());

        SunType type = SunType.NORMAL;
        double rand = random.nextDouble();
        if (rand < 0.05) {
            type = SunType.RADIOACTIVE;
        } else if (rand < 0.20) {
            type = SunType.SPECIAL;
        }

        String typeName = switch (type) {
            case RADIOACTIVE -> "radioactive";
            case SPECIAL -> "special";
            case NORMAL -> "regular";
        };

        gameContext.log("New " + typeName + " sun is dropping at position (" + col + ", " + lane + ")");

        Sun sun = new Sun(type, col, lane, type.getAmountSun(), true, gameContext);
        gameContext.spawnSun(sun);
    }

    @Override
    public void dispose() {
    }
}
