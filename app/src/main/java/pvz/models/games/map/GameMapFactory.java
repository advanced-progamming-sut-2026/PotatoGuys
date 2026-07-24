package pvz.models.games.map;

import pvz.models.entities.plants.Plant;
import pvz.models.games.map.behaviors.DestructibleBehavior;
import pvz.models.games.map.behaviors.IceBlockBehavior;
import pvz.models.games.map.behaviors.SlipperyBehavior;
import pvz.models.games.map.behaviors.WaterBehavior;
import pvz.models.games.map.data.BehaviorDefinition;
import pvz.models.games.map.data.GameMapDefinition;
import pvz.models.games.map.data.TileDefinition;
import pvz.models.games.map.tile.TileTags;

public class GameMapFactory {
    public static GameMap createGameMap(GameMapDefinition def) {
        GameMap map = new GameMap(def.rows, def.columns);
        if (def.specialTiles != null) {
            for (TileDefinition tileDef : def.specialTiles) {
                if (tileDef.behaviors != null) {
                    for (BehaviorDefinition behDef : tileDef.behaviors) {
                        switch (behDef.type) {
                            case DESTRUCTIBLE -> map.getTile(tileDef.x, tileDef.y)
                                    .addBehavior(new DestructibleBehavior(behDef.hp, behDef.name));
                            case SLIPPERY ->
                                map.getTile(tileDef.x, tileDef.y).addBehavior(new SlipperyBehavior(behDef.laneDelta));
                            case WATER -> map.getTile(tileDef.x, tileDef.y).addBehavior(new WaterBehavior());
                            case NECROMANCY -> {
                                map.getTile(tileDef.x, tileDef.y).getTags().add(TileTags.NECROMANCY);
                                if (behDef.hp > 0) {
                                    map.getTile(tileDef.x, tileDef.y).addBehavior(new DestructibleBehavior(behDef.hp,
                                            behDef.name != null ? behDef.name : "Grave"));
                                }
                            }
                            case ICE_BLOCK -> {
                                // Ice blocks need to contain something, this might need more data in JSON
                                // For now, assume it freezes the plant on the tile
                                for (Plant p : map.getTile(tileDef.x, tileDef.y).getPlants()) {
                                    map.getTile(tileDef.x, tileDef.y).addBehavior(new IceBlockBehavior(p));
                                }
                            }
                        }
                    }
                }
                if (tileDef.tags != null) {
                    for (TileTags tag : tileDef.tags) {
                        map.getTile(tileDef.x, tileDef.y).getTags().add(tag);
                    }
                }
            }
        }

        return map;
    }
}
