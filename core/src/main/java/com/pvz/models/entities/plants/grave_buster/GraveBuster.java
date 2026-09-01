package com.pvz.models.entities.plants.grave_buster;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.grave_buster.state.Init;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class GraveBuster extends Plant {
    public static final String PAM_PATH="768/INITIAL/PLANT/GRAVEBUSTER/GRAVEBUSTER.PAM";
    public static final float DEFAULT_EAT_DURATION=2.8f;
    public GraveBuster(PlantPropertySheet sheet, PlantAction attackAction,
            PlantAction feedAction, int col, int lane,
            int level, boolean boosted, GameContext context) {
        super(sheet, attackAction, feedAction, col, lane, level, boosted, context);
        this.changeState(new Init());
    }

    @Override
    public boolean isPlantableOnTile(Tile tile) {
        return tile.getTags().contains(TileTags.GRAVE);
    }
}
