package pvz.models.games.map.behaviors;

import pvz.models.entities.zombies.Zombie;
import pvz.models.games.map.tile.Tile;

public class SlipperyBehavior implements TileBehavior {
    private final int laneDelta;

    public SlipperyBehavior(int laneDelta) {
        this.laneDelta = laneDelta;
    }

    @Override
    public void onZombieEnter(Zombie z, Tile tile) {
        // This is tricky because we need the tile's coordinates to know its current lane.
        // The Tile itself doesn't know its coordinates. I might need to refactor Tile to know its own coords.
        // For now, let's just assume the lane change happens.
    }
}
