package com.pvz.models.games.map.tile;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.behaviors.DestructibleBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;

public class Tile {
    private final int lane;
    private final int col;
    private List<TileTags> tags;
    private List<TileBehavior> behaviors = new ArrayList<>();
    private List<Plant> plants;

    public Tile(int lane, int col) {
        this.lane=lane;
        this.col=col;
        plants = new ArrayList<>();
        tags = new ArrayList<>();
    }

    public boolean isPlantable(PlantCard newPlant) {
        if (newPlant.getPlant().getType() == PlantType.HotPotato) {
            return true;
        }
        if (newPlant.getPlant().getType() == PlantType.GraveBuster) {
            boolean hasGrave = tags.contains(TileTags.GRAVE) || behaviors.stream()
                    .anyMatch(b -> b instanceof DestructibleBehavior);
            if (!hasGrave)
                return false;
        }


        for (TileBehavior b : behaviors) {
            if (!b.canPlant(newPlant, this))
                return false;
        }

        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(newPlant.getPlant().getType());
        if (!plants.isEmpty() && !sheet.getTags().contains(PlantTag.STACK)) {
            boolean hasLilyPad = plants.stream().anyMatch(p -> p.getType() == PlantType.LilyPad);
            if (!hasLilyPad) {
                return false;
            }
            else if (plants.size()<2){
                return true;
            }
        }

        if (!plants.isEmpty()) return false;

        return true;
    }

    public List<TileBehavior> getBehaviors() {
        return behaviors;
    }

    public void addBehavior(TileBehavior b) {
        behaviors.add(b);
    }

    public void removeBehavior(TileBehavior b) {
        behaviors.remove(b);
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void addPlant(Plant plant) {
        plants.add(plant);
    }

    public void removePlant(Plant plant) {
        plants.remove(plant);
    }

    public void processHit(Projectile p) {
        for (TileBehavior b : new ArrayList<>(behaviors))
            b.onProjectileHit(p, this);
    }

    public void onZombieEnter(Zombie z) {
        for (TileBehavior b : new ArrayList<>(behaviors))
            b.onZombieEnter(z, this);
    }

    public void onTick(GameContext ctx) {
        for (TileBehavior b : new ArrayList<>(behaviors))
            b.onTick(ctx, this);
    }

    public List<TileTags> getTags() {
        return tags;
    }
}
