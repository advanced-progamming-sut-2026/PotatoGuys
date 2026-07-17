package pvz.Models.Games;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Sun.SunManager;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.Modes.GameModeFactory;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.map.GameMap;
import pvz.Models.Games.map.Tile;


public class GameContext implements TickAware {
    private int currentSun;
    private int currentTick;
    private int levelNumber;
    private boolean gameOver;
    int plantFoodCount;

    private final GameEngine engine;
    private List<Projectile> projectiles;
    private List<Zombie> zombies;
    private List<Plant> plants;
    private List<Card> cards;
    private List<Sun> suns;
    private GameMode mode;
    private GameMap map;

    public GameContext(Level currentLevel) {
        this.engine         = GameEngine.getInstance();
        this.engine.register(this);
        this.currentSun     = currentLevel.getInitialSun();
        this.zombies        = new ArrayList<>();
        this.plants         = new ArrayList<>();
        this.projectiles    = new ArrayList<>();
        this.suns           = new ArrayList<>();
        this.map = currentLevel.getGameMap();
        this.mode = GameModeFactory.createGameMode(currentLevel);
        this.setLevelNumber(currentLevel.getLevelNumber());
        this.plantFoodCount=0;
        engine.register(new SunManager(this));
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
        return getZombies().stream()
                .filter(z -> z.getLane() == lane && (int) z.getX() == col && !z.isDead())
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
        engine.register(z);
    }
    public boolean removeZombie(Zombie z) {
        engine.unRegister(z);
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
        if(!getTileAt(p.getCol(), p.getLane()).isPlantable(p)) {
            log("Cant spawn this plant at " + p.getCol() + " " + p.getLane());
            return;
        }
        getTileAt(p.getCol(), p.getLane()).addPlant(p);
        engine.register(p);
        plants.add(p);
    }
    public boolean isPlantAt(int col, int lane) {
        return !getPlantsAt(col, lane).isEmpty();
    }
    public boolean removePlant(Plant p) {
        getTileAt(p.getCol(), p.getLane()).removePlant(p);
        engine.unRegister(p);
        return plants.remove(p);
    }

    public List<Card> getCards() {
        return cards;
    }
    public void addCard(Card card){
        this.cards.add(card);
        engine.register(card);
    }
    public void removeCard(Card card){
        this.cards.remove(card);
        engine.unRegister(card);
    }
    public void setCards(List<Card> cards) {
        for(Card card : cards){
            addCard(card);
        }
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }
    public void spawnProjectile(Projectile p) {
        projectiles.add(p);
        engine.register(p);
    }
    public boolean removeProjectile(Projectile p) {
        engine.unRegister(p);
        return projectiles.remove(p);
    }

    public List<Sun> getSuns() {
        return suns;
    }
    public void spawnSun(Sun s) {
        engine.register(s);
        suns.add(s);
    }
    public boolean removeSun(Sun s) {
        engine.unRegister(s);
        return suns.remove(s);
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public void addCurrentTick(int amount){
        this.currentTick += amount;
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

    public Tile getTileAt(float col , int lane){
        return map.getTile((int)col, lane);
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

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getPlantFoodCount(){
        return plantFoodCount;
    }

    public void addPlantFood(int amount){
        plantFoodCount+=amount;
        if (plantFoodCount>4){
            plantFoodCount=4;
        }
    }

    public boolean spendPlantFood(){
        if (plantFoodCount<=0) return false;
        plantFoodCount--;
        return true;
    }
}
