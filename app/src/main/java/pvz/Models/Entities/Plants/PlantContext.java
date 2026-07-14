package pvz.Models.Entities.Plants;

import java.util.List;

import pvz.Models.Entities.Plants.Enums.PlantCategory;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;

/**
 * Decouples plant/projectile logic from the concrete game world, exactly the
 * way {@link pvz.Models.Entities.Zombies.GameContext} decouples zombies.
 *
 * <p>Every interaction a plant has with the board — finding zombies to
 * attack, spending/earning sun, spawning projectiles or floating suns,
 * broadcasting a Mint's family-wide Plant Food buff — goes through this
 * interface. The concrete adapter (a thin wrapper around
 * {@link pvz.Models.Seasons.Levels.GameMap}, or a demo/test double) implements
 * both {@code GameContext} and {@code PlantContext} so zombies and plants
 * observe a single, consistent world.
 */
public interface PlantContext {

    // ─── Grid info ────────────────────────────────────────────────────────────

    int getColumns();
    int getLanes();

    /** True if a living plant occupies (col, lane). */
    boolean isPlantAt(int col, int lane);

    // ─── Zombie queries (used by attacking plants) ───────────────────────────

    /** True if at least one living zombie is in {@code lane}. */
    boolean hasZombieInLane(int lane);

    /** All living zombies currently in {@code lane}, in no particular order. */
    List<Zombie> getZombiesInLane(int lane);

    /**
     * Nearest living zombie in {@code lane} at or beyond {@code col} (i.e. the
     * first zombie this plant's projectiles would reach), or {@code null}.
     */
    Zombie getNearestZombieAhead(int col, int lane);

    /** Any living zombie anywhere on the board, or {@code null} (for Homing plants). */
    Zombie getAnyZombieOnBoard();

    // ─── Damage delivery ──────────────────────────────────────────────────────

    /**
     * Deals damage directly to a zombie (used by melee/instant effects that
     * don't need a travelling {@link Projectile}).
     *
     * @param poisonous if true, bypasses the zombie's armour chain
     */
    void dealDamageToZombie(Zombie zombie, float damage, boolean poisonous);

    // ─── Entity spawning ──────────────────────────────────────────────────────

    /** Registers a new projectile with the engine (mirrors {@code GameContext.spawnZombie}). */
    void spawnProjectile(Projectile projectile);

    /** Registers a newly produced floating sun with the engine. */
    void spawnSun(Sun sun);

    // ─── Sun economy ──────────────────────────────────────────────────────────

    int getSunAmount();
    void addSun(int amount);

    /** Attempts to deduct {@code amount}; returns false (no-op) if insufficient funds. */
    boolean trySpendSun(int amount);

    // ─── Mint family-buff support ─────────────────────────────────────────────

    /** All living plants of the given category currently on the board (a Mint's "family"). */
    List<Plant> getActivePlantsInFamily(PlantCategory family);

    // ─── Console output ───────────────────────────────────────────────────────

    void log(String message);
}
