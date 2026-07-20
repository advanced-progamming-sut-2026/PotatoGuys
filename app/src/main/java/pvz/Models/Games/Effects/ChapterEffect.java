package pvz.Models.Games.Effects;

import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Wave;

public interface ChapterEffect {
    default void onTick(GameContext ctx) {}
    default void onWaveStart(Wave wave, GameContext ctx) {}
}
