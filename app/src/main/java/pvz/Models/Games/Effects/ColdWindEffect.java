package pvz.Models.Games.Effects;

import pvz.Models.Games.GameContext;
import pvz.Models.Entities.Plants.Plant;
import java.util.Random;

public class ColdWindEffect implements ChapterEffect {
    private final int intervalTicks;
    private int tickCounter = 0;
    private final Random rand = new Random();

    public ColdWindEffect(int intervalTicks) {
        this.intervalTicks = intervalTicks;
    }

    @Override
    public void onTick(GameContext ctx) {
        tickCounter++;
        if (tickCounter >= intervalTicks) {
            triggerColdWind(ctx);
            tickCounter = 0;
        }
    }

    private void triggerColdWind(GameContext ctx) {
        // Frostbite Caves only
        if (!"frostbite caves".equalsIgnoreCase(ctx.getSeasonName())) return;
        
        // Random rows
        int numLanes = ctx.getLanes();
        int affectedLanes = rand.nextInt(numLanes) + 1; // 1 to all lanes
        
        for (int i = 0; i < affectedLanes; i++) {
            int lane = rand.nextInt(numLanes);
            ctx.log("[ColdWind] Cold wind blowing in lane " + lane);
            for (Plant p : ctx.getPlantsInLane(lane)) {
                p.incrementFreezeLevel();
            }
        }
    }
}
