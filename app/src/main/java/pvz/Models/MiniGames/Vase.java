package pvz.Models.MiniGames;

import pvz.Models.Plants.Plant;
import pvz.Models.Zombies.Zombie;

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
