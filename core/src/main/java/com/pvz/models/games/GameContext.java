package com.pvz.models.games;

import java.util.ArrayList;
import java.util.List;

import com.pvz.controller.game.GameController;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.engine.TickAware;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.Hitbox;
import com.pvz.models.entities.LawnMower;
import com.pvz.models.entities.effects.Effect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.sun.SunManager;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.effects.ChapterEffect;
import com.pvz.models.games.effects.EffectFactory;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.data.EffectDefinition;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.GameMapFactory;
import com.pvz.models.games.map.behaviors.IceBlockBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.data.PrePlantedPlant;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.GameModeFactory;


public class GameContext implements TickAware {
    private int currentSun;
    private int currentTick;
    private int levelNumber;
    private boolean gameOver;
    private int plantFoodCount;

    private final GameEngine engine;
    private List<Projectile> projectiles;
    private List<Zombie> zombies;
    private List<Plant> plants;
    private List<Card> cards;
    private List<Sun> suns;
    private List<Effect> effects;
    private LawnMower[] lawnMowers;
    private final List<Hitbox> hitboxes = new ArrayList<>();
    private GameMode mode;
    private GameMap map;
    private GameStats gameStats;
    private String seasonName;
    private List<ChapterEffect> activeEffects;

    // ── Deferred mutations ─────────────────────────────────────────────────────
    // spawnX / removeX never touch the live lists directly: they queue here and
    // {@link #flushPending()} applies the queues after collision detection (and
    // again at the start of each engine tick). This guarantees entity lists are
    // never mutated while they are being iterated (which threw
    // ConcurrentModificationException in the collision system).
    private final List<Zombie> pendingZombiesToAdd = new ArrayList<>();
    private final List<Zombie> pendingZombiesToRemove = new ArrayList<>();
    private final List<Plant> pendingPlantsToAdd = new ArrayList<>();
    private final List<Plant> pendingPlantsToRemove = new ArrayList<>();
    private final List<Projectile> pendingProjectilesToAdd = new ArrayList<>();
    private final List<Projectile> pendingProjectilesToRemove = new ArrayList<>();
    private final List<Effect> pendingEffectsToAdd = new ArrayList<>();
    private final List<Effect> pendingEffectsToRemove = new ArrayList<>();
    private final List<Sun> pendingSunsToAdd = new ArrayList<>();
    private final List<Sun> pendingSunsToRemove = new ArrayList<>();

    public GameContext(Level currentLevel) {
        this.engine         = GameEngine.getInstance();
        this.engine.register(this);
        this.currentSun     = currentLevel.getInitialSun();
        this.zombies        = new ArrayList<>();
        this.plants         = new ArrayList<>();
        this.projectiles    = new ArrayList<>();
        this.suns           = new ArrayList<>();
        this.effects        = new ArrayList<>();
        this.cards          = new ArrayList<>();
        this.map = GameMapFactory.createGameMap(currentLevel.getGameMapDefinition());
        this.lawnMowers=new LawnMower[map.getLanes()];
        for (int i = 0; i < lawnMowers.length; i++) {
            lawnMowers[i]=new LawnMower(this,i);
            hitboxes.add(lawnMowers[i].getHitbox());
            GameEngine.getInstance().getToAdd().add(lawnMowers[i]);
        }
        this.mode = GameModeFactory.createGameMode(currentLevel);
        this.setLevelNumber(currentLevel.getLevelNumber());
        this.seasonName     = currentLevel.getSeasonName();

        // Spawn pre-planted plants
        if (currentLevel.getGameMapDefinition().prePlantedPlants != null) {
            for (PrePlantedPlant pDef : currentLevel.getGameMapDefinition().prePlantedPlants) {
                Plant p = new PlantFactory().create(
                    pDef.type, pDef.col, pDef.lane, 1, false, this
                );
                if (currentLevel.getGameMapDefinition().specialTiles.stream().anyMatch(t->(
                        t.tags.contains(TileTags.ICE_BLOCK) && t.x==p.getCol() && t.y==p.getLane()))){
                    p.incrementFreezeLevel();
                    p.incrementFreezeLevel();
                    p.incrementFreezeLevel();
                }
                this.spawnPlant(p);
            }
        }

        this.log("DEBUG: GameContext initialized with season: '" + this.seasonName + "'");
        this.plantFoodCount=0;
        this.gameStats=new GameStats();
        engine.register(new SunManager(this));

        this.activeEffects = new ArrayList<>();
        if (currentLevel.getEffects() != null) {
            for (EffectDefinition def : currentLevel.getEffects()) {
                ChapterEffect effect = EffectFactory.createEffect(def);
                if (effect != null) {
                    activeEffects.add(effect);
                }
            }
        }
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
        gameStats.onSunCollected(amount);
    }

    public void decreaseSun(int amount){
        currentSun -= amount;
    }

    public boolean spendSun(int amount) {
        if (currentSun < amount) return false;
        currentSun -= amount;
        return true;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public List<Zombie> getZombiesAt(int col, int lane) {
        return getZombies().stream()
                .filter(z -> GameController.worldYtoLane(z.getY()) == lane && GameController.worldXtoCol(z.getX()) == col && !z.isDead())
                .toList();
    }

    public List<Zombie> getZombiesInLane(int lane) {
        return zombies.stream()
                .filter(z -> GameController.worldYtoLane(z.getY()) == lane && !z.isDead())
                .toList();
    }

    public List<Zombie> getZombiesInColumn(int col) {
        return zombies.stream()
                .filter(z -> GameController.worldXtoCol(z.getX()) == col && !z.isDead())
                .toList();
    }

    public void spawnZombie(Zombie z) {
        pendingZombiesToAdd.add(z);
        engine.register(z);
        addHitbox(z.getHitbox());
    }

    public boolean removeZombie(Zombie z) {
        engine.unRegister(z);
        pendingZombiesToRemove.add(z);
        removeHitbox(z.getHitbox());
        return true;
    }

    public boolean isZombieAt(int col, int lane) {
        return !getZombiesAt(col, lane).isEmpty();
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public List<Plant> getPlantsAt(int col, int lane) {
        Tile tile = map.getTileAt(col, lane);
        if (tile == null) return List.of();
        return tile.getPlants().stream()
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
        getTileAt(p.getCol(), p.getLane()).addPlant(p);
        pendingPlantsToAdd.add(p);
        engine.register(p);
        addHitbox(p.getHitbox());
    }

    public boolean isPlantAt(int col, int lane) {
        return !getPlantsAt(col, lane).isEmpty();
    }

    public boolean removePlant(Plant p) {
        getTileAt(p.getCol(), p.getLane()).removePlant(p);
        engine.unRegister(p);
        pendingPlantsToRemove.add(p);
        removeHitbox(p.getHitbox());
        return true;
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
        pendingProjectilesToAdd.add(p);
        engine.register(p);
        addHitbox(p.getHitbox());
    }

    public boolean removeProjectile(Projectile p) {
        engine.unRegister(p);
        pendingProjectilesToRemove.add(p);
        removeHitbox(p.getHitbox());
        return true;
    }

    public List<Sun> getSuns() {
        return suns;
    }

    public void spawnSun(Sun s) {
        pendingSunsToAdd.add(s);
        engine.register(s);
        addHitbox(s.getHitbox());
    }
    public boolean removeSun(Sun s) {
        engine.unRegister(s);
        pendingSunsToRemove.add(s);
        removeHitbox(s.getHitbox());
        return true;
    }

    // ── Hitbox registry ─────────────────────────────────────────────────────────
    // Every spawned entity registers its hitbox here so the CollisionSystem can
    // sweep all hitboxes in one pass without caring about entity types.

    public void addHitbox(Hitbox hitbox) {
        if (hitbox != null) {
            hitboxes.add(hitbox);
        }
    }

    public void removeHitbox(Hitbox hitbox) {
        if (hitbox != null) {
            hitboxes.remove(hitbox);
        }
    }

    public List<Hitbox> getHitboxes() {
        return hitboxes;
    }

    /**
     * Applies every queued spawn/remove operation. Called by the engine right
     * after collision detection (and once again at the start of each tick), so
     * the live entity lists are never mutated mid-iteration.
     */
    public void flushPending() {
        zombies.addAll(pendingZombiesToAdd);
        zombies.removeAll(pendingZombiesToRemove);
        pendingZombiesToAdd.clear();
        pendingZombiesToRemove.clear();

        plants.addAll(pendingPlantsToAdd);
        plants.removeAll(pendingPlantsToRemove);
        pendingPlantsToAdd.clear();
        pendingPlantsToRemove.clear();

        projectiles.addAll(pendingProjectilesToAdd);
        projectiles.removeAll(pendingProjectilesToRemove);
        pendingProjectilesToAdd.clear();
        pendingProjectilesToRemove.clear();

        suns.addAll(pendingSunsToAdd);
        suns.removeAll(pendingSunsToRemove);
        pendingSunsToAdd.clear();
        pendingSunsToRemove.clear();

        effects.addAll((pendingEffectsToAdd));
        effects.removeAll(pendingEffectsToRemove);
        pendingEffectsToAdd.clear();
        pendingEffectsToRemove.clear();
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
        return map.getTileAt((int)col, lane);
    }

    public void log(String message) { System.out.println("  " + message); }

    public GameStats getGameStats() { return gameStats; }

    @Override
    public void enter() {
        mode.initMode(this);
    }


    @Override
    public void update(float dt) {
        applyFireAuras();
        for (ChapterEffect effect : activeEffects) {
            effect.update(this,dt);
        }
        mode.updateMode(this , dt);
    }

    private void applyFireAuras() {
        for (Plant p : plants) {
            if (p.getSheet().hasTag(PlantTag.FIRE)) {
                // Find neighbors
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int neighborCol = p.getCol() + dx;
                        int neighborLane = p.getLane() + dy;
                        if (neighborCol >= 0 && neighborCol < map.getColumns() &&
                            neighborLane >= 0 && neighborLane < map.getLanes()) {

                            Tile tile = getTileAt(neighborCol, neighborLane);

                            // 1. Damage frozen plants
                            List<Plant> neighbors = tile.getPlants();
                            for (Plant neighbor : neighbors) {
                                if (neighbor.isFrozen()) {
                                    neighbor.takeIceDamage(60f / Plant.TICKS_PER_SECOND, true);
                                }
                            }

                            // 2. Damage IceBlockBehaviors
                            for (TileBehavior b : tile.getBehaviors()) {
                                if (b instanceof IceBlockBehavior ice) {
                                    ice.takeDamage(60f / Plant.TICKS_PER_SECOND, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void dispose() {}

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public List<ChapterEffect> getActiveEffects() {
        return activeEffects;
    }

    public int getPlantFoodCount(){
        return plantFoodCount;
    }

    public void addPlantFood(int amount){
        plantFoodCount+=amount;
        if (plantFoodCount>3){
            plantFoodCount=3;
        }
    }

    public boolean spendPlantFood(){
        if (plantFoodCount<=0) return false;
        plantFoodCount--;
        return true;
    }

    public LawnMower[] getLawnMowers(){
        return lawnMowers;
    }

    public List<Effect> getEffects(){
        return effects;
    }

    public void addEffect(Effect effect){
        pendingEffectsToAdd.add(effect);
        engine.getToAdd().add(effect);
    }

    public void removeEffect(Effect effect){
        pendingEffectsToRemove.add(effect);
        engine.getToRemove().add(effect);
    }
}
