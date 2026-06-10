package Models.MiniGames;

import java.lang.classfile.instruction.ArrayLoadInstruction;
import java.util.ArrayList;

import Models.Plants.Plant;
import Models.Zombies.Zombie;


public class IZombie extends MiniGame{
    private ArrayList<Plant> placedPlants;
    private ArrayList<Zombie> availableZombies;
    private ArrayList<Brain> brains;
    private int redLine;
    private void placeZombie(Tile tile , Zombie zombie);
    private boolean canPlaceZombie(Tile tile);
    private void setupBoard();
    public void onFirstTick();
    public void onTick();
}
