package pvz.models.games.map.behaviors;

import pvz.models.entities.zombies.Zombie;
import pvz.models.games.card.PlantCard;
import pvz.models.games.map.tile.Tile;

public class SlipperyBehavior implements TileBehavior {
    private final int laneDelta;

    public SlipperyBehavior(int laneDelta) {
        this.laneDelta = laneDelta;
    }

    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
        if (p != null && p.getPlant().getType() == pvz.models.entities.plants.enums.PlantType.HotPotato) {
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
