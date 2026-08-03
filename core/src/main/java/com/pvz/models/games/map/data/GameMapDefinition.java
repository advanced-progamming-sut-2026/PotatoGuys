package com.pvz.models.games.map.data;

import java.util.List;

public class GameMapDefinition {
    public int rows = 5;
    public int columns = 9;
    public List<TileDefinition> specialTiles;
    public List<PrePlantedPlant> prePlantedPlants;
}
