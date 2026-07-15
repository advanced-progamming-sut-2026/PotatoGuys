package pvz.Models.TestGameContext.mode;

import pvz.Models.TestGameContext.GameContext;

public interface GameMode {
    boolean gameIsOver();
    void tick(GameContext ctx);
}
