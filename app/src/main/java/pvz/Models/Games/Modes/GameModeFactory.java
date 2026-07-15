package pvz.Models.Games.Modes;

import pvz.Models.Games.Levels.Level;

public class GameModeFactory {
    public static GameMode createGameMode(GameModeType modeType , Level level) {
        switch (modeType) {
            case IZOMBIE:
                return new IZombieMode(level);
            case NORMAL:
                return new NormalMode(level);
            default:
                throw new IllegalArgumentException("Unknown game mode: " + modeType.toString());
        }
    }
}
