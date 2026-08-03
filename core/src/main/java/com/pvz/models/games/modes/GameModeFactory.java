package com.pvz.models.games.modes;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.modes.variants.*;

public class GameModeFactory {
    public static GameMode createGameMode(Level level) {
        switch (level.getGameMode()) {
            case NORMAL:
                return new NormalMode(level);
            case CONVEYORBELT:
                return new ConveyorBeltMode(level);
            case TIMEDWAR:
                return new TimedWarMode(level);
            case IZOMBIE:
                return new IZombieMode(level);
            case PLANTWHATYOUGET:
                return new PlantWhatYouGetMode(level);
            case DEADLINE:
                return new DeadLineMode(level);
            case VASEBREAKER:
                return new VaseBreakerMode(level);
            case BEGHOULED:
                return new BeghouledMode(level);
            case WALLNUTBOWLING:
                return new WallnutBowlingMode(level);
            default:
                throw new IllegalArgumentException("Unknown game mode: " + level.getGameMode().toString());
        }
    }
}
