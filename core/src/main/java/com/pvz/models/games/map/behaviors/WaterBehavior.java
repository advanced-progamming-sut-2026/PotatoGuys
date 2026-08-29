package com.pvz.models.games.map.behaviors;

import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;

public class WaterBehavior implements TileBehavior {
    float stateTime;

    @Override
    public boolean canPlant(PlantCard card, Tile tile) {
        if (card == null)
            return false;
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        if (sheet == null)
            return false;

        // If tile is empty, can plant directly if plant has WATER tag (e.g. LilyPad,
        // Seashroom, TangleKelp)
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
    public String getName() {
        return "Water";
    }
}
