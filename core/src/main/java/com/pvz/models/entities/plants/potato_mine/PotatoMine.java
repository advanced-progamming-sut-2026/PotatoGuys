package com.pvz.models.entities.plants.potato_mine;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.GameMap;

public class PotatoMine extends Plant {
    public static final String PAM_PATH="768/INITIAL/PLANT/POTATOMINE/POTATOMINE.PAM";
    public static final float PLANT_DURATION = 1f;
    public static final float IDLE_DURATION = 10f;
    public static final float RECOVER_DURATION = 0.8f;
    public static final float ATTACK_RANGE = GameMap.TILE_WIDTH;
    public static final float ATTACK_DURATION = 0.67f;
    public static final float BASE_DAMAGE = 1800f;
    public PotatoMine(PlantPropertySheet sheet, PlantAction attackAction, PlantAction feedAction, int col, int lane, int level, boolean boosted, GameContext context) {
        super(sheet, attackAction, feedAction, col, lane, level, boosted, context);
    }
}
