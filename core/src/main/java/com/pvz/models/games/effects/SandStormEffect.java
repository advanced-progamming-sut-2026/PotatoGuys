package com.pvz.models.games.effects;

import com.pvz.models.games.GameContext;

public class SandStormEffect implements ChapterEffect {
    private final int intervalTicks;

    public SandStormEffect(int intervalTicks) {
        this.intervalTicks = intervalTicks;
    }

    @Override
    public void update(GameContext ctx, float dt) {
        // Sandstorm visual feedback is handled by Wave.spawnZombie
    }
}
