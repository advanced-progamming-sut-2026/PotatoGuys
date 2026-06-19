package Models.MiniGames;

import java.lang.classfile.instruction.ArrayLoadInstruction;
import java.util.ArrayList;
import java.util.logging.Level;

import Models.Plants.Plant;
import Models.Seasons.Levels.Tile;
import Models.Zombies.Zombie;


public class IZombie extends Level{
    private ArrayList<Plant> placedPlants;
    private ArrayList<Zombie> availableZombies;
    private ArrayList<Brain> brains;
    private int redLine;
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
    private boolean canPlaceZombie(Tile tile){}
    private void setupBoard(){}
    public void onFirstTick(){}
    public void onTick(){}
}
