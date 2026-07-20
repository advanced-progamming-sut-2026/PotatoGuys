package pvz.Models.Games.Modes;

import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Modes.variants.BeghouledMode;
import pvz.Models.Games.Modes.variants.ConveyorBeltMode;
import pvz.Models.Games.Modes.variants.DeadLineMode;
import pvz.Models.Games.Modes.variants.NormalMode;
import pvz.Models.Games.Modes.variants.VasebreakerMode;

public class GameModeFactory {
    public static GameMode createGameMode(Level level) {
        switch (level.getGameMode()) {
            case NORMAL:
                return new NormalMode(level);
            case CONVEYORBELT:
                return new ConveyorBeltMode(level);
            case DEADLINE:
                return new DeadLineMode(level);
            case VASEBREAKER:
                return new VasebreakerMode(level);
            case BEGHOULED:
                return new BeghouledMode(level);
            default:
                throw new IllegalArgumentException("Unknown game mode: " + level.getGameMode().toString());
        }
    }
}
