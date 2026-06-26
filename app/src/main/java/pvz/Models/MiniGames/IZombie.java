package pvz.Models.MiniGames;

import java.util.ArrayList;
import java.util.logging.Level;

import pvz.Models.Plants.Plant;
import pvz.Models.Seasons.Levels.Tile;
import pvz.Models.Zombies.Zombie;


public class IZombie extends Level{
    private ArrayList<Plant> placedPlants;
    private ArrayList<Zombie> availableZombies;
    private ArrayList<Brain> brains;
    private int redLine;

    protected IZombie(String name, int value) {
        super(name, value);
    }

    public ArrayList<Plant> getPlacedPlants() {
        return placedPlants;
    }
    public ArrayList<Zombie> getAvailableZombies() {
        return availableZombies;
    }
    public ArrayList<Brain> getBrains() {
        return brains;
    }
    public int getRedLine() {
        return redLine;
    }
    private void placeZombie(Tile tile , Zombie zombie){}
    private boolean canPlaceZombie(Tile tile){
        return true;
    }
    private void setupBoard(){}
    public void onFirstTick(){}
    public void onTick(){}
}
