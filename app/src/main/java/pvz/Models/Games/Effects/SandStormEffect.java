package pvz.Models.Games.Effects;

import pvz.Models.Games.GameContext;

public class SandStormEffect implements ChapterEffect {
    private final int intervalTicks;

    public SandStormEffect(int intervalTicks) {
        this.intervalTicks = intervalTicks;
    }

    @Override
    public void onTick(GameContext ctx) {
        // Sandstorm visual feedback is handled by Wave.spawnZombie
    }
}
