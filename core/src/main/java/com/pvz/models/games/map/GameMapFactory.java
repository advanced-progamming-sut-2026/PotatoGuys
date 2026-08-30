package com.pvz.models.games.map;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.behaviors.IceBlockBehavior;
import com.pvz.models.games.map.behaviors.SlipperyBehavior;
import com.pvz.models.games.map.behaviors.WaterBehavior;
import com.pvz.models.games.map.data.BehaviorDefinition;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.map.data.TileDefinition;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class GameMapFactory {
    public static GameMap createGameMap(GameMapDefinition def) {
        GameMap map = new GameMap(def.rows, def.columns);
        if (def.specialTiles != null) {
            for (TileDefinition tileDef : def.specialTiles) {
                if (tileDef.behaviors != null) {
                    for (BehaviorDefinition behDef : tileDef.behaviors) {
                        Tile tile = map.getTileAt(tileDef.x, tileDef.y);
                        switch (behDef.type) {
                            case DESTRUCTIBLE ->
                                tile.addBehavior(new GraveBehavior(tile, behDef.hp, behDef.name));
                            case SLIPPERY ->
                                tile.addBehavior(new SlipperyBehavior(behDef.laneDelta));
                            case WATER ->
                                tile.addBehavior(new WaterBehavior());
                            case NECROMANCY -> {
                                tile.getTags().add(TileTags.NECROMANCY);
                                if (behDef.hp > 0) {
                                    tile.addBehavior(new GraveBehavior(tile, behDef.hp,
                                            behDef.name != null ? behDef.name : "Grave"));
                                }
                            }
                            case ICE_BLOCK -> {
                                // Ice blocks need to contain something, this might need more data in JSON
                                // For now, assume it freezes the plant on the tile
                                for (Plant p : tile.getPlants()) {
                                    tile.addBehavior(new IceBlockBehavior(tile, p));
                                }
                            }
                            case LOW_TIDE -> {
                            }
                        }
                    }
                }
                if (tileDef.tags != null) {
                    for (TileTags tag : tileDef.tags) {
                        map.getTileAt(tileDef.x, tileDef.y).getTags().add(tag);
                    }
                }
            }
        }

        return map;
    }
}
