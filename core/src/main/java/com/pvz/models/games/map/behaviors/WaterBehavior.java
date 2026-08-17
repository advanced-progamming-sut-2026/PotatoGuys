package com.pvz.models.games.map.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;

public class WaterBehavior implements TileBehavior {
    private static final String PAM_PATH = "768/FULL/BACKGROUNDS/WATER_SQUARE/WATER_SQUARE.PAM";
    private static final String CLIP = "Water";

    float stateTime;

    @Override
    public boolean canPlant(PlantCard card, Tile tile) {
        if (card == null) return false;
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        if (sheet == null) return false;

        // If tile is empty, can plant directly if plant has WATER tag (e.g. LilyPad, Seashroom, TangleKelp)
        if (tile.getPlants().isEmpty()) {
            return sheet.getTags().contains(PlantTag.WATER);
        }

        // If tile already has plants, check if there is a Lily Pad on it
        boolean hasLilyPad = tile.getPlants().stream().anyMatch(p -> p.getType() == PlantType.LilyPad);
        if (hasLilyPad) {
            return true; // Can stack on Lily Pad
        }

        return sheet.getTags().contains(PlantTag.STACK);
    }

    @Override
    public void update(GameContext ctx, Tile tile, float dt) {
        TileBehavior.super.update(ctx, tile, dt);
        stateTime += dt;
    }

    @Override
    public FrameConfig draw(Tile tile) {
        TileBehavior.super.draw(tile);
        Vector2 pos = new Vector2(GameController.colToWorldX(tile.getCol()), GameController.laneToWorldY(tile.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        return new FrameConfig(PAM_PATH, CLIP, stateTime, pos, scale, null, true);
    }

    @Override
    public String getName() {
        return "Water";
    }
}
