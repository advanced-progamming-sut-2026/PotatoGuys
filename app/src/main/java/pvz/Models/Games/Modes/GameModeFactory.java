package pvz.Models.Games.Modes;

public class GameModeFactory {
    public static GameMode createGameMode(GameModeType modeType) {
        switch (modeType) {
            case IZOMBIE:
                return new IZombieMode();
            case STANDARD:
                return new StandardMode();
            default:
                throw new IllegalArgumentException("Unknown game mode: " + modeType.toString());
        }
    }
}
