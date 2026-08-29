package com.pvz.models.games.effects;

import java.util.List;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.levels.Wave;

public interface ChapterEffect {
    default void update(GameContext ctx, float dt) {
    }

    default void onWaveStart(Wave wave, GameContext ctx) {
    }

    default void first(GameContext ctx) {
    }

    default List<FrameConfig> draw() {
        return null;
    }
}
