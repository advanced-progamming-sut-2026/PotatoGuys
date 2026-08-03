package com.pvz.models.games.map.behaviors;

import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;

public class SlipperyBehavior implements TileBehavior {
    private final int laneDelta;

    public SlipperyBehavior(int laneDelta) {
        this.laneDelta = laneDelta;
    }

    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
        if (p != null && p.getPlant().getType() == PlantType.HotPotato) {
            return true;
        }
        return false;
    }

    @Override
    public void onZombieEnter(Zombie z, Tile tile) {
        int newLane = z.getLane() + laneDelta;
        // Basic check to ensure lane stays in bounds
        if (newLane >= 0 && newLane < 5) {
            z.setLane(newLane);
        }
    }

    @Override
    public String getName() { return "Slippery"; }
}
