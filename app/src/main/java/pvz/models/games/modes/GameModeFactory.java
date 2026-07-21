package pvz.models.games.modes;
import pvz.models.games.levels.Level;
import pvz.models.games.modes.variants.BeghouledMode;
import pvz.models.games.modes.variants.ConveyorBeltMode;
import pvz.models.games.modes.variants.DeadLineMode;
import pvz.models.games.modes.variants.IZombieMode;
import pvz.models.games.modes.variants.NormalMode;
import pvz.models.games.modes.variants.PlantWhatYouGetMode;
import pvz.models.games.modes.variants.TimedWarMode;
import pvz.models.games.modes.variants.VasebreakerMode;

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
                return new VasebreakerMode(level);
            case BEGHOULED:
                return new BeghouledMode(level);
            default:
                throw new IllegalArgumentException("Unknown game mode: " + level.getGameMode().toString());
        }
    }
}
