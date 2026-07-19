package pvz.Models.Games.Levels.Data;

import pvz.Models.Games.map.TileTags;

import java.util.List;

public class TileDefinition {
    public int x;
    public int y;
    public List<BehaviorDefinition> behaviors;
    public List<TileTags> tags;
}
