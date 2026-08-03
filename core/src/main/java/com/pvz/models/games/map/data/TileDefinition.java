package com.pvz.models.games.map.data;

import java.util.List;

import com.pvz.models.games.map.tile.TileTags;

public class TileDefinition {
    public int x;
    public int y;
    public List<BehaviorDefinition> behaviors;
    public List<TileTags> tags;
}
