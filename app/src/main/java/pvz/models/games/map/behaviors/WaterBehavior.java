package pvz.models.games.map.behaviors;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.PlantPropertySheet;
import pvz.models.entities.plants.data.PlantRegistry;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.games.card.PlantCard;
import pvz.models.games.map.tile.Tile;

public class WaterBehavior implements TileBehavior {

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
    public String getName() {
        return "Water";
    }
}
