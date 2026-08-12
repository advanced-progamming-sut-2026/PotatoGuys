package com.pvz.models.entities.plants.bonk_choy;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.bonk_choy.state.Idle;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class BonkChoy extends Plant {
    public static final String PAM_PATH="768/INITIAL/PLANT/BONKCHOY/BONKCHOY.PAM";
    public static final float DEFAULT_ACTION_INTERVAL = 0.25f;
    public static final float BASE_DAMAGE = 15;
    public static final float DEFAULT_ATTACK_RANGE= GameMap.TILE_WIDTH*1.6f;
    public BonkChoy(PlantPropertySheet sheet, PlantAction attackAction, PlantAction feedAction, int col, int lane, int level, boolean boosted, GameContext context) {
        super(sheet, attackAction, feedAction, col, lane, level, boosted, context);
        this.changeState(new Idle());
    }
}
