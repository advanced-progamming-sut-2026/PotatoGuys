package pvz.Demo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.GameContext;
import pvz.Models.Entities.Plants.Enums.PlantCategory;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.GameContext;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieFactory;

/**
 * Full-board world adapter for the CLI demo: implements both
 * {@link GameContext} (what {@link Zombie} sees) and {@link GameContext}
 * (what {@link Plant}/{@link Projectile} see), so a single object mediates
 * every cross-system interaction — Plants shoot Zombies, Zombies eat Plants,
 * Suns get collected, exactly as {@link pvz.Models.Engine.GameEngine} drives
 * all of them through the same tick loop.
 */
public class PlantWarContext implements GameContext, GameContext {

    public static final int COLS = 9;
    public static final int LANES = 5;

    private int sun;
    private boolean gameOver;
    private final boolean[] lawnMowerUsed = new boolean[LANES];

    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Sun> suns = new ArrayList<>();

    private final GameEngine engine;
    private final ZombieFactory zombieFactory = new ZombieFactory();

    public PlantWarContext(GameEngine engine, int startingSun) {
        this.engine = engine;
        this.sun = startingSun;
    }

    // ── Shared grid info (GameContext & PlantContext) ─────────────────────────

    @Override public int getColumns() { return COLS; }
    @Override public int getLanes()   { return LANES; }

    @Override
    public boolean isPlantAt(int col, int lane) {
        return findPlant(col, lane) != null;
    }

    @Override public int getSunAmount() { return sun; }

    @Override
    public void log(String message) { System.out.println("  " + message); }

    // ── GameContext (zombie-facing) ───────────────────────────────────────────

    @Override
    public void dealDamageToPlant(int col, int lane, float damage, boolean poisonous) {
        Plant p = findPlant(col, lane);
        if (p != null) p.takeDamage(damage);
    }

    @Override public boolean isTorchVulnerablePlantAt(int col, int lane) { return false; }
    @Override public void burnPlant(int col, int lane) { }
    @Override public void transformPlantToCat(int col, int lane) { }
    @Override public void applyFrostToPlant(int col, int lane) { }
    @Override public void applyOctopusToPlant(int col, int lane) { }
    @Override public boolean isWaterAt(int col, int lane) { return false; }

    @Override public void stealSun(int amount)  { sun = Math.max(0, sun - amount); }
    @Override public void returnSun(int amount) { sun += amount; }

    @Override
    public void raiseTomb(int col, int lane) { log("[TOMB] Tomb raised at (" + col + "," + lane + ")"); }

    @Override
    public int[] getRandomEmptyCell() {
        return new int[]{(int) (Math.random() * COLS), (int) (Math.random() * LANES)};
    }

    @Override
    public void spawnZombie(String alias, int col, int lane) {
        try {
            Zombie z = zombieFactory.create(alias, col, lane, this, 0, 3);
            zombies.add(z);
            engine.register(z);
            log("[SPAWN] " + alias + " at (" + col + "," + lane + ")");
        } catch (IllegalArgumentException e) {
            log("[ERROR] Unknown zombie alias: " + alias);
        }
    }

    @Override
    public void triggerLawnMower(int lane) {
        if (!lawnMowerUsed[lane]) {
            lawnMowerUsed[lane] = true;
            log(">>> LAWN MOWER in lane " + lane + " ACTIVATED!");
            for (Zombie z : zombies) {
                if (z.getLane() == lane && !z.isDead()) z.takeDamage(Float.MAX_VALUE);
            }
        } else {
            gameOver = true;
            log(">>> A zombie reached the house in lane " + lane + "! GAME OVER.");
        }
    }

    // ── PlantContext (plant/projectile-facing) ────────────────────────────────

    @Override
    public boolean hasZombieInLane(int lane) {
        return zombies.stream().anyMatch(z -> !z.isDead() && z.getLane() == lane);
    }

    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie z : zombies) if (!z.isDead() && z.getLane() == lane) result.add(z);
        return result;
    }

    @Override
    public Zombie getNearestZombieAhead(int col, int lane) {
        Zombie nearest = null;
        for (Zombie z : zombies) {
            if (z.isDead() || z.getLane() != lane || z.getX() < col) continue;
            if (nearest == null || z.getX() < nearest.getX()) nearest = z;
        }
        return nearest;
    }

    @Override
    public Zombie getAnyZombieOnBoard() {
        for (Zombie z : zombies) if (!z.isDead()) return z;
        return null;
    }

    @Override
    public void dealDamageToZombie(Zombie zombie, float damage, boolean poisonous) {
        zombie.takeDamage(damage, poisonous);
    }

    @Override
    public void spawnProjectile(Projectile projectile) {
        projectiles.add(projectile);
        engine.register(projectile);
    }

    @Override
    public void spawnSun(Sun sunEntity) {
        suns.add(sunEntity);
        engine.register(sunEntity);
    }

    @Override
    public void addSun(int amount) { sun += amount; }

    @Override
    public boolean trySpendSun(int amount) {
        if (sun < amount) return false;
        sun -= amount;
        return true;
    }

    @Override
    public List<Plant> getActivePlantsInFamily(PlantCategory family) {
        List<Plant> result = new ArrayList<>();
        for (Plant p : plants) if (!p.isDead() && p.getSheet().getCategory() == family) result.add(p);
        return result;
    }

    // ── Demo bookkeeping ───────────────────────────────────────────────────────

    public boolean isGameOver() { return gameOver; }

    public void addPlant(Plant p) { plants.add(p); engine.register(p); }

    public Plant findPlant(int col, int lane) {
        for (Plant p : plants) {
            if (!p.isDead() && p.getCol() == col && p.getLane() == lane) return p;
        }
        return null;
    }

    public List<Plant> getPlants()           { return plants; }
    public List<Zombie> getZombies()         { return zombies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Sun> getSuns()               { return suns; }

    /** Collects the first uncollected sun at (col, lane), if any. */
    public boolean collectSunAt(int col, int lane) {
        for (Sun s : suns) {
            if (!s.isDone() && s.getCol() == col && s.getLane() == lane) {
                s.collect(this);
                return true;
            }
        }
        return false;
    }

    /** Removes dead/spent/expired entities from the engine after a tick batch. */
    public void cleanup() {
        removeDead(plants, Plant::isDead);
        removeDead(zombies, Zombie::isDead);
        removeIf(projectiles, Projectile::isSpent);
        removeIf(suns, Sun::isDone);
    }

    private <T> void removeDead(List<T> list, java.util.function.Predicate<T> isDead) {
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            T item = it.next();
            if (isDead.test(item)) {
                engine.unRegister((pvz.Models.Engine.TickAware) item);
                it.remove();
            }
        }
    }

    private <T> void removeIf(List<T> list, java.util.function.Predicate<T> pred) {
        removeDead(list, pred);
    }
}
