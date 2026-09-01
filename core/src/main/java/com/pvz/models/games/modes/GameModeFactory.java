package com.pvz.models.games.modes;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.modes.variants.BeghouledMode;
import com.pvz.models.games.modes.variants.ConveyorBeltMode;
import com.pvz.models.games.modes.variants.DeadLineMode;
import com.pvz.models.games.modes.variants.IZombieMode;
import com.pvz.models.games.modes.variants.IZombieLocalMode;
import com.pvz.models.games.modes.variants.NormalMode;
import com.pvz.models.games.modes.variants.SaveOurSeedsMode;
import com.pvz.models.games.modes.variants.ScoredMode;
import com.pvz.models.games.modes.variants.TimedWarMode;
import com.pvz.models.games.modes.variants.VaseBreakerMode;
import com.pvz.models.games.modes.variants.WallnutBowlingMode;

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
            case SPLIT_IZOMBIE:
                return new IZombieLocalMode(level);
            case SAVEOURSEEDS:
                return new SaveOurSeedsMode(level);
            case DEADLINE:
                return new DeadLineMode(level);
            case VASEBREAKER:
                return new VaseBreakerMode(level);
            case BEGHOULED:
                return new BeghouledMode(level);
            case WALLNUTBOWLING:
                return new WallnutBowlingMode(level);
            case SCORED:
                return new ScoredMode(level);
            default:
                throw new IllegalArgumentException("Unknown game mode: " + level.getGameMode().toString());
        }
    }
}
