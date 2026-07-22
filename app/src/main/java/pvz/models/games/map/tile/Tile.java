package pvz.models.games.map.tile;

import java.util.ArrayList;
import java.util.List;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.PlantPropertySheet;
import pvz.models.entities.plants.data.PlantRegistry;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.projectile.Projectile;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;
import pvz.models.games.card.PlantCard;
import pvz.models.games.map.behaviors.TileBehavior;

public class Tile {
    private List<TileTags> tags;
    private List<TileBehavior> behaviors = new ArrayList<>();
    private List<Plant> plants;

    public Tile(){
        plants=new ArrayList<>();
        tags=new ArrayList<>();
    }

    public boolean isPlantable(PlantCard newPlant) {
        for (TileBehavior b : behaviors) {
            if (!b.canPlant(newPlant, this)) return false;
        }

        PlantPropertySheet sheet= PlantRegistry.getInstance().getSheet(newPlant.getPlant().getType());
        if(!plants.isEmpty() && !sheet.getTags().contains(PlantTag.STACK)){
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
            plants.add(plant);
    }

    public void removePlant(Plant plant) {
        plants.remove(plant);
    }

    public void processHit(Projectile p) {
        for (TileBehavior b : new ArrayList<>(behaviors)) b.onProjectileHit(p, this);
    }

    public void onZombieEnter(Zombie z) {
        for (TileBehavior b : new ArrayList<>(behaviors)) b.onZombieEnter(z, this);
    }

    public void onTick(GameContext ctx) {
        for (TileBehavior b : new ArrayList<>(behaviors)) b.onTick(ctx, this);
    }

    public List<TileTags> getTags(){
        return tags;
    }
}
