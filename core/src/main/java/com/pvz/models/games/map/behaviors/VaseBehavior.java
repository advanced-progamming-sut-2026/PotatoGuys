package com.pvz.models.games.map.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.data.VaseType;
import com.pvz.models.games.map.tile.Tile;

public class VaseBehavior implements TileBehavior {
    private static final String BROWN_PAM = "768/FULL/VASEBREAKER/VASE_BROWN/VASE_BROWN.PAM";
    private static final String GREEN_PAM = "768/FULL/VASEBREAKER/VASE_GREEN/VASE_GREEN.PAM";
    private static final String GARGANTUAR_PAM = "768/FULL/VASEBREAKER/VASE_GARGANTUAR/VASE_GARGANTUAR.PAM";

    private static final String IDLE_CLIP = "idle";
    private static final String BREAK_CLIP = "break";

    float stateTime;
    private final VaseType vaseType;
    private boolean broken;
    private boolean breakAnimDone;
    private Tile tile;

    public VaseBehavior(VaseType vaseType, Tile tile) {
        this.vaseType = vaseType;
        this.broken = false;
        this.breakAnimDone = false;
        this.stateTime = 0;
        this.tile = tile;
        tile.addBehavior(this);
    }

    public VaseType getVaseType() {
        return vaseType;
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean isBreakAnimDone() {
        return breakAnimDone;
    }

    public void breakVase() {
        if (!broken) {
            broken = true;
            stateTime = 0;
        }
    }

    @Override
    public void update(GameContext ctx, Tile tile, float dt) {
        TileBehavior.super.update(ctx, tile, dt);
        stateTime += dt;
        if (broken && !breakAnimDone) {
            float breakDuration = 1.8f;
            if (stateTime >= breakDuration) {
                breakAnimDone = true;
                dispose();
            }
        }
    }

    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
        return !broken || breakAnimDone;
    }

    @Override
    public FrameConfig draw(Tile tile) {
        TileBehavior.super.draw(tile);
        if (broken && breakAnimDone)
            return null;

        String pamPath = BROWN_PAM;
        switch (vaseType) {
            case NORMAL -> pamPath = BROWN_PAM;
            case PLANT -> pamPath = GREEN_PAM;
            case GARGANTUAR -> pamPath = GARGANTUAR_PAM;
        }
        String clip = broken ? BREAK_CLIP : IDLE_CLIP;

        Vector2 pos = new Vector2(
                GameController.colToWorldX(tile.getCol()),
                GameController.laneToWorldY(tile.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);

        return new FrameConfig(pamPath, clip, stateTime, pos, scale, null, false);
    }

    @Override
    public String getName() {
        return "Vase";
    }

    @Override
    public void dispose() {
        tile.removeBehavior(this);
    }
}
