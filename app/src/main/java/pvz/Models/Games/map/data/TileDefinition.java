package pvz.Models.Games.map.data;

import java.util.List;

import pvz.Models.Games.map.tile.TileTags;

public class TileDefinition {
    public int x;
    public int y;
    public List<BehaviorDefinition> behaviors;
    public List<TileTags> tags;
}
