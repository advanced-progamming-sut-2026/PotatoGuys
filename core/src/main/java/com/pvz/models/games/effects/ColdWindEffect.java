package com.pvz.models.games.effects;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.entities.effects.ChillWind;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

public class ColdWindEffect implements ChapterEffect {
    private final float intervalTime;
    private float stateTime = 0;
    private final Random rand = new Random();

    public ColdWindEffect(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    @Override
    public void update(GameContext ctx, float dt) {
        stateTime+=dt;
        if (stateTime >= intervalTime) {
            triggerColdWind(ctx);
            stateTime = 0;
        }
    }

    private void triggerColdWind(GameContext ctx) {
        // Frostbite Caves only
        if (!"frostbite caves".equalsIgnoreCase(ctx.getSeasonName())) return;

        // Random rows
        int numLanes = ctx.getMap().getLanes();
        int affectedLanes = rand.nextInt(numLanes) + 1; // 1 to all lanes

        for (int i = 0; i < affectedLanes; i++) {
            int lane = rand.nextInt(numLanes);

            int midCol = ctx.getMap().getColumns()/2;
            Vector2 pos = new Vector2(GameController.colToWorldX(midCol),GameController.laneToWorldY(lane));
            ctx.addEffect(new ChillWind(ctx, pos));
            ctx.log("[ColdWind] Cold wind blowing in lane " + lane);
            for (Plant p : ctx.getPlantsInLane(lane)) {
                p.incrementFreezeLevel();
            }
        }
    }
}
