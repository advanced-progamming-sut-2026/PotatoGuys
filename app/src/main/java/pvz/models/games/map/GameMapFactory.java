package pvz.models.games.map;

import pvz.models.games.map.behaviors.DestructibleBehavior;
import pvz.models.games.map.behaviors.SlipperyBehavior;
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
                            case DESTRUCTIBLE -> map.getTile(tileDef.x, tileDef.y).addBehavior(new DestructibleBehavior(behDef.hp, behDef.name));
                            case SLIPPERY -> map.getTile(tileDef.x, tileDef.y).addBehavior(new SlipperyBehavior(behDef.laneDelta));
                        }
                    }
                }
                if (tileDef.tags !=null){
                    for (TileTags tag: tileDef.tags){
                        map.getTile(tileDef.x,tileDef.y).getTags().add(tag);
                    }
                }
            }
        }

        return map;
    }
}
