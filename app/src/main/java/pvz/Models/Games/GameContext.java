package pvz.Models.Games;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.Modes.GameModeFactory;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.map.GameMap;


public class GameContext implements TickAware {
    private int currentSun;
    private int currentTick;
    private boolean gameOver;

    private final GameEngine engine;
    private List<Zombie> zombies;
    private List<Plant> plants;
    private List<Projectile> projectiles;
    private List<Sun> suns;
    private GameMode mode;
    private GameMap map;

    public GameContext(Level currentLevel) {
        this.engine         = GameEngine.getInstance();
        this.currentSun     = currentLevel.getInitialSun();
        this.zombies        = new ArrayList<>();
        this.plants         = new ArrayList<>();
        this.projectiles    = new ArrayList<>();
        this.suns           = new ArrayList<>();
        this.map = currentLevel.getGameMap();
        this.mode = GameModeFactory.createGameMode(currentLevel.getGameMode() , currentLevel);
    }


    public GameEngine getEngine() {
        return engine;
    }

    public GameMode getMode() {
        return mode;
    }

    public int getCurrentSun() {
        return currentSun;
    }
    public void addSun(int amount) {
        currentSun += amount;
    }
    public void decreaseSun(int amount){
        currentSun -= amount;
    }
    public boolean spendSun(int amount) {
        if (currentSun < amount) return false;
        currentSun -= amount;
        return true;
    }

    public int getColumns() {
        return map.getColumns();
    }
    public int getLanes() {
        return map.getRows();
    }

    public List<Zombie> getZombies() {
        return zombies;
    }
    public List<Zombie> getZombiesAt(int col, int lane) {
        return map.getTile(col, lane).getZombies().stream()
                .filter(z -> !z.isDead())
                .toList();
    }
    public List<Zombie> getZombiesInLane(int lane) {
        return zombies.stream()
                .filter(z -> z.getLane() == lane && !z.isDead())
                .toList();
    }
    public List<Zombie> getZombiesInColumn(int col) {
        return zombies.stream()
                .filter(z -> (int) z.getX() == col && !z.isDead())
                .toList();
    }
    public void spawnZombie(Zombie z) {
        zombies.add(z);
    }
    public boolean removeZombie(Zombie z) {
        return zombies.remove(z);
    }
    public boolean isZombieAt(int col, int lane) {
        return !getZombiesAt(col, lane).isEmpty();
    }
    public List<Plant> getPlants() {
        return plants;
    }
    public List<Plant> getPlantsAt(int col, int lane) {
        return map.getTile(col, lane).getPlants().stream()
                .filter(p -> !p.isDead())
                .toList();
    }
    public List<Plant> getPlantsInLane(int lane) {
        return plants.stream()
                .filter(p -> p.getLane() == lane && !p.isDead())
                .toList();
    }
    public List<Plant> getPlantsInColumn(int col) {
        return plants.stream()
                .filter(p -> p.getCol() == col && !p.isDead())
                .toList();
    }
    public void spawnPlant(Plant p) {
        plants.add(p);
    }
    public boolean isPlantAt(int col, int lane) {
        return !getPlantsAt(col, lane).isEmpty();
    }
    public boolean removePlant(Plant p) {
        return plants.remove(p);
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }
    public void spawnProjectile(Projectile p) {
        projectiles.add(p);
    }
    public boolean removeProjectile(Projectile p) {
        return projectiles.remove(p);
    }

    public List<Sun> getSuns() {
        return suns;
    }
    public void spawnSun(Sun s) {
        suns.add(s);
    }
    public boolean removeSun(Sun s) {
        return suns.remove(s);
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }


    public boolean isGameOver() {
        return gameOver;
    }


    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public GameMap getMap() {
        return map;
    }

    public void setMap(GameMap map) {
        this.map = map;
    }

    public void log(String message) { System.out.println("  " + message); }

    @Override
    public void enter() {
        mode.initMode(this);
    }


    @Override
    public void update() {
        mode.updateMode(this);
    }


    @Override
    public void dispose() {}
}
