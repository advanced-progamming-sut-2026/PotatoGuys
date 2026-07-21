package pvz.Models.Games.map.behaviors;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.Games.map.tile.Tile;

public class SlipperyBehavior implements TileBehavior {
    private final int laneDelta;

    public SlipperyBehavior(int laneDelta) {
        this.laneDelta = laneDelta;
    }

    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
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
