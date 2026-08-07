package com.pvz.models.entities.plants.peashooter;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.games.GameContext;

public class Peashooter extends Plant {
    public Peashooter(PlantPropertySheet sheet, PlantAction action, int col, int lane, int level, boolean boosted, GameContext context) {
        super(sheet, action, col, lane, level, boosted, context);
    }
}
