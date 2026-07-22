package pvz.models.games.effects;

import pvz.models.games.GameContext;
import pvz.models.games.levels.Wave;

public interface ChapterEffect {
    default void onTick(GameContext ctx) {}
    default void onWaveStart(Wave wave, GameContext ctx) {}
}
