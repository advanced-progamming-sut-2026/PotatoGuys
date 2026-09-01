package com.pvz.models.games.map.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.map.tile.Tile;

public class ProtectTileBehavior implements TileBehavior {
    private static final String PAM_PATH = "768/INITIAL/BACKGROUNDS/PROTECT_TILE/PROTECT_TILE.PAM";
    private static final String CLIP = "animation";

    private float stateTime;

    public ProtectTileBehavior() {
        this.stateTime = 0;
    }

    @Override
    public void update(com.pvz.models.games.GameContext ctx, Tile tile, float dt) {
        stateTime += dt;
    }

    @Override
    public FrameConfig draw(Tile tile) {
        Vector2 pos = new Vector2(
                tile.getX() + Tile.WIDTH / 2f,
                tile.getY() + Tile.HEIGHT / 2f);
        Vector2 scale = new Vector2(0.65f, 0.65f);
        return new FrameConfig(PAM_PATH, CLIP, stateTime, pos, scale, null, true);
    }

    @Override
    public String getName() {
        return "ProtectTile";
    }
}
