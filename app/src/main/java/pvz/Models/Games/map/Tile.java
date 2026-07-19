package pvz.Models.Games.map;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.map.behaviors.TileBehavior;

public class Tile {
    private List<TileBehavior> behaviors = new ArrayList<>();
    private List<Plant> plants;

    public Tile(){
        plants=new ArrayList<>();
    }

    public boolean isPlantable(Plant newPlant) {
        for (TileBehavior b : behaviors) {
            if (!b.canPlant(newPlant, this)) return false;
        }

        if(!plants.isEmpty() && !newPlant.getSheet().getTags().contains(PlantTag.STACK)){
            return false;
        }
        return true;
    }

    public List<TileBehavior> getBehaviors() { return behaviors; }
    public void addBehavior(TileBehavior b) { behaviors.add(b); }
    public void removeBehavior(TileBehavior b) { behaviors.remove(b); }

    public List<Plant> getPlants() {
        return plants;
    }

    public void addPlant(Plant plant) {
        if(this.isPlantable(plant) && !plants.contains(plant))
            plants.add(plant);
    }

    public void removePlant(Plant plant) {
        plants.remove(plant);
    }

    public void processHit(Projectile p) {
        for (TileBehavior b : behaviors) b.onProjectileHit(p, this);
    }

    public void onZombieEnter(Zombie z) {
        for (TileBehavior b : behaviors) b.onZombieEnter(z, this);
    }

    public void onTick(GameContext ctx) {
        for (TileBehavior b : behaviors) b.onTick(ctx, this);
    }
}
