package com.pvz.models.games.effects;

import com.pvz.models.games.GameContext;
import com.pvz.models.games.levels.Wave;

public interface ChapterEffect {
    default void update(GameContext ctx, float dt) {}
    default void onWaveStart(Wave wave, GameContext ctx) {}
}
