package com.pvz.models.entities.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

/**
 * Renders a helm armour piece that is tossed off the zombie's head like the
 * death-head effect, but over a much shorter arc. It is thrown in a short
 * gravity parabola (a little upward pop with a small horizontal drift), then
 * fades and removes itself.
 *
 * <p>The visibility map force-shows the armour's damage-layer parts (e.g. the
 * critical {@code _damage_02} stage — the last damaged look the armour had on
 * the head) plus any container part, and hides the rest of the zombie sheet so
 * only the helmet is visible as it flies.
 */
public class DetachedArmorEffect extends Effect {

    private static final float TOTAL_TIME = 0.9f;
    private static final float GRAVITY = 500f;
    private static final float MAX_COLUMNS = 0.8f;

    private final String pamPath;
    private final float scale;
    private final Map<String, Boolean> partsVisibility;
    private final float vx;
    private final float vy;
    private final float startX;

    public DetachedArmorEffect(GameContext ctx, Vector2 pos, String pamPath,
            float scale, List<String> armorShowParts,
            List<String> hidePartNames) {
        super(ctx, pos);
        this.pamPath = pamPath;
        this.scale = scale;
        this.startX = pos.x;

        float distance = MathUtils.random(-MAX_COLUMNS, MAX_COLUMNS) * GameMap.TILE_WIDTH;
        vx = distance / TOTAL_TIME;
        vy = 0.5f * GRAVITY * TOTAL_TIME;

        this.partsVisibility = new HashMap<>();

        if (hidePartNames != null) {
            for (String part : hidePartNames) {
                partsVisibility.put(part, false);
            }
        }
        for (String part : armorShowParts) {
            partsVisibility.put(part, true);
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (stateTime >= TOTAL_TIME) {
            dispose();
            return;
        }
        pos.x = startX + vx * stateTime;
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        float height = vy * stateTime - 0.5f * GRAVITY * stateTime * stateTime;
        Vector2 drawPos = new Vector2(pos.x, pos.y + height);
        float alpha = 1f;
        if (stateTime > TOTAL_TIME * 0.6f) {
            alpha = 1f - (stateTime - TOTAL_TIME * 0.6f) / (TOTAL_TIME * 0.4f);
            alpha = Math.max(0f, alpha);
        }
        FrameConfig fc = new FrameConfig(pamPath, "idle", stateTime,
                drawPos, new Vector2(scale, scale), partsVisibility, true);
        fc.setColor(1f, 1f, 1f, alpha);
        frameConfigs.add(fc);
        return frameConfigs;
    }
}