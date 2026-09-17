package com.pvz.models.entities.sun;

import java.util.Random;

import com.pvz.models.engine.TickAware;
import com.pvz.models.games.GameContext;

public class SunManager implements TickAware {
    private final GameContext gameContext;
    private float secondsSinceLastDrop;
    private float stateTime;
    private final Random random = new Random();

    public SunManager(GameContext gameContext) {
        this.gameContext = gameContext;
        this.secondsSinceLastDrop = 0;
        this.stateTime = 0;
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(float dt) {
        stateTime+=dt;
        secondsSinceLastDrop+=dt;

        if (!gameContext.getMode().supportsFallingSuns()) {
            return;
        }
        if ("dark ages".equalsIgnoreCase(gameContext.getSeasonName())) {
            return;
        }

        double xSeconds = Math.min(4 + 0.05 * stateTime, 6.5);

        if (secondsSinceLastDrop >= xSeconds) {
            spawnSun();
            secondsSinceLastDrop = 0;
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
