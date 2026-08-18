package com.pvz.models.games.map.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;

public class SlipperyBehavior implements TileBehavior {
    private static final String UP_PAM_PATH="768/FULL/EFFECTS/TILESLIDER_ICEAGE_UP/TILESLIDER_ICEAGE_UP.PAM";
    private static final String DOWN_PAM_PATH="768/FULL/EFFECTS/TILESLIDER_ICEAGE_UP/TILESLIDER_ICEAGE_UP.PAM";
    private static final String IDLE_CLIP="idle";

    private static final float SLIP_DURATION=1.5f;

    private final int laneDelta;
    private final String pamPath;
    private String currentClip=IDLE_CLIP;
    float stateTime=0;

    public SlipperyBehavior(int laneDelta) {
        this.laneDelta = laneDelta;

        if (laneDelta>0) pamPath=UP_PAM_PATH;
        else pamPath=DOWN_PAM_PATH;
    }

    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
        return false;
    }

    @Override
    public void onZombieEnter(Zombie z, Tile tile) {
        int newLane = GameController.worldYtoLane(z.getY()) + laneDelta;
        // Basic check to ensure lane stays in bounds
        if (newLane >= 0 && newLane < 5) {
            z.setY(GameController.laneToWorldY(newLane));
        }
    }

    @Override
    public void update(GameContext ctx, Tile tile, float dt) {
        TileBehavior.super.update(ctx, tile, dt);
        stateTime+=dt;
    }

    @Override
    public FrameConfig draw(Tile tile) {
        Vector2 pos = new Vector2(tile.getX()+Tile.WIDTH/2,tile.getY()+Tile.HEIGHT/2);
        return new FrameConfig(pamPath,currentClip,stateTime,pos,
            new Vector2(0.65f,0.65f),null,true);
    }

    @Override
    public String getName() { return "Slippery"; }
}
