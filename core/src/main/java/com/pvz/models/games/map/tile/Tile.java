package com.pvz.models.games.map.tile;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.engine.TickAware;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;

public class Tile implements TickAware {
    public static final float WIDTH = 82f;
    public static final float HEIGHT = 96f;
    private final int lane;
    private final int col;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private List<TileTags> tags;
    private List<TileBehavior> behaviors = new ArrayList<>();
    private List<Plant> plants;

    public Tile(int col, int lane, float x, float y, float width, float height) {
        this.col = col;
        this.lane = lane;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        plants = new ArrayList<>();
        tags = new ArrayList<>();
    }

    public boolean isPlantable(PlantCard newPlant) {
        if (newPlant.getPlant().getType() == PlantType.HotPotato) {
            return true;
        }
        if (newPlant.getPlant().getType() == PlantType.GraveBuster) {
            boolean hasGrave = tags.contains(TileTags.GRAVE) || behaviors.stream()
                    .anyMatch(b -> b instanceof GraveBehavior);
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

    public void processHit(float damage){
        for (TileBehavior b : new ArrayList<>(behaviors))
            b.processHit(damage);
    }

    public void onZombieEnter(Zombie z) {
        for (TileBehavior b : new ArrayList<>(behaviors))
            b.onZombieEnter(z, this);
    }

    public List<TileTags> getTags() {
        return tags;
    }

    public int getLane() {
        return lane;
    }

    public int getCol() {
        return col;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public void enter() {

    }

    @Override
    public void update(float dt) {
        for (TileBehavior b : new ArrayList<>(behaviors))
            b.update(null, this,dt);
    }

    @Override
    public FrameConfig draw() {
        TickAware.super.draw();
        for (TileBehavior b: new ArrayList<>(behaviors)){
            b.draw(this);
        }
        return null;
    }

    @Override
    public void dispose() {

    }
}
