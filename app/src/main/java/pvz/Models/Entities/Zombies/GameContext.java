package pvz.Models.Entities.Zombies;

/**
 * Decouples zombie logic from the concrete game world.
 *
 * <p>Every interaction a zombie has with the board (checking for plants,
 * dealing damage, spawning new entities, logging output) is mediated
 * through this interface. This makes the zombie system fully testable
 * and prevents circular dependencies between the Models layers.
 *
 * <p>The {@link pvz.Models.Seasons.Levels.GameMap} (or a thin adapter
 * around it) should implement this interface and be passed into every
 * {@link Zombie} at construction time via {@link ZombieFactory}.
 */
public interface GameContext {

    // ─── Plant queries ────────────────────────────────────────────────────────

    /** Returns true if a living, non-cat, non-transformed plant occupies (col, lane). */
    boolean isPlantAt(int col, int lane);

    /**
     * Deals {@code damage} to whatever plant is at (col, lane).
     * Does nothing if the cell is empty.
     *
     * @param poisonous if true, damage bypasses armor (Poison Pea behaviour)
     */
    void dealDamageToPlant(int col, int lane, float damage, boolean poisonous);

    /**
     * Returns true if the plant at (col, lane) is vulnerable to an open flame
     * (e.g. Frost Bonnet). Used by the Explorer Zombie torch mechanic.
     */
    boolean isTorchVulnerablePlantAt(int col, int lane);

    /** Instantly destroys the plant at (col, lane) via fire — Explorer skill. */
    void burnPlant(int col, int lane);

    /** Turns the plant at (col, lane) into a harmless cat — Wizard Zombie skill. */
    void transformPlantToCat(int col, int lane);

    /**
     * Adds one frost level to the plant at(col, lane).
     * At 3 stacked levels the plant becomes fully frozen (Hunter Zombie).
     */
    void applyFrostToPlant(int col, int lane);

    /**
     * Applies an octopus effect to the plant at (col, lane), immobilising it
     * (similar to ice-freezing) — Octopus Zombie skill.
     */
    void applyOctopusToPlant(int col, int lane);

    // ─── Sun economy ──────────────────────────────────────────────────────────

    /** Current player sun reserve. */
    int getSunAmount();

    /** Deducts {@code amount} from the player's sun (Ra Zombie theft). */
    void stealSun(int amount);

    /**
     * Returns stolen sun back to the player
     * (called on Ra Zombie death or when wizard is killed).
     */
    void returnSun(int amount);

    // ─── Tomb / grid manipulation ─────────────────────────────────────────────

    /**
     * Places a new tomb at (col, lane). No-op if the cell already has a tomb
     * or is occupied by a plant.
     */
    void raiseTomb(int col, int lane);

    /**
     * Returns the [col, lane] of a random empty grid cell, or null if
     * none exists (used by TombRaiser to pick random spawn locations).
     */
    int[] getRandomEmptyCell();

    // ─── Zombie spawning ──────────────────────────────────────────────────────

    /**
     * Spawns a new zombie identified by its registry alias at (col, lane).
     * Used by Gargantuar to throw an Imp and by Troglobite internally.
     *
     * @param alias the JSON alias string, e.g. {@code "ZombieEgyptImpDefault"}
     */
    void spawnZombie(String alias, int col, int lane);

    // ─── Map information ──────────────────────────────────────────────────────

    /** Number of columns (typically 9). */
    int getColumns();

    /** Number of lanes / rows (typically 5). */
    int getLanes();

    /** True if the tile at (col, lane) is underwater. */
    boolean isWaterAt(int col, int lane);

    // ─── Lawn mower ───────────────────────────────────────────────────────────

    /**
     * Triggers the lawn mower in {@code lane}.
     * First call kills all zombies in that row; second call ends the game.
     */
    void triggerLawnMower(int lane);

    // ─── Console output ───────────────────────────────────────────────────────

    /** Appends a message to the game's CLI output stream. */
    void log(String message);
}
