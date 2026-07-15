package pvz.Models.toDel.MiniGames;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.Zombie;

public class Vase {
    private VaseType type;
    private Zombie zombie;
    private Plant plant;
    public VaseType getType() {
        return type;
    }
    public Zombie getZombie() {
        return zombie;
    }
    public Plant getPlant() {
        return plant;
    }
}
