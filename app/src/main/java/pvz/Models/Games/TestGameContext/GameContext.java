package pvz.Models.TestGameContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieFactory;
import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Seasons.Levels.GameMap;


public abstract class GameContext implements TickAware {

    public static final int COLS;
    public static final int LANES;

    private int currentSun;
    private int currentTick;
    private boolean gameOver;

    private final boolean[] lawnMowerUsed;
    private List<Zombie> zombies;
    private List<Plant> plants;
    private List<Projectile> projectiles;
    private List<Sun> suns;
    private List<Card> cards;
    private GameMap map;

    private final GameEngine engine;


    public GameContext() {
        this.engine         = GameEngine.getInstance();
        this.currentSun            = 50;
        this.lawnMowerUsed  = new boolean[LANES];
        this.cards          = new ArrayList<>();
        this.zombies        = new ArrayList<>();
        this.plants         = new ArrayList<>();
        this.projectiles    = new ArrayList<>();
        this.suns           = new ArrayList<>();
        this.map            = new GameMap(COLS, LANES);
    }

    // ── ZombieGameContext ─────────────────────────────────────────────────────

    // @Override public boolean isPlantAt(int col, int lane)                          { return false; }
    // @Override public void dealDamageToPlant(int c, int l, float d, boolean p)      {}
    // @Override public boolean isTorchVulnerablePlantAt(int col, int lane)           { return false; }
    // @Override public void burnPlant(int col, int lane)                             {}
    // @Override public void transformPlantToCat(int col, int lane)                   {}
    // @Override public void applyFrostToPlant(int col, int lane)                     {}
    // @Override public void applyOctopusToPlant(int col, int lane)                   {}
    // @Override public boolean isWaterAt(int col, int lane)                          { return false; }
    // @Override public int getColumns()                                              { return COLS; }
    // @Override public int getLanes()                                                { return LANES; }
    // @Override public int getSunAmount()                                            { return sun; }
    // @Override public void stealSun(int amount)   { sun = Math.max(0, sun - amount); }
    // @Override public void returnSun(int amount)  { sun += amount; }

    // @Override
    // public void raiseTomb(int col, int lane) {
    //     log("[TOMB] Tomb raised at (" + col + "," + lane + ")");
    // }

    // @Override
    // public int[] getRandomEmptyCell() {
    //     return new int[]{(int)(Math.random() * COLS), (int)(Math.random() * LANES)};
    // }

    @Override
    public void spawnZombie(ZombieType type, int col, int lane) {
        try {
            Zombie z = (new ZombieFactory()).create(type.getAlias(), col, lane, this, 0, 3);
            zombies.add(z);
            engine.register(z);
        } catch (IllegalArgumentException e) {
            log("[ERROR] Unknown alias: " + type.getAlias());
        }
    }

    @Override
    public void triggerLawnMower(int lane) {
        if (!lawnMowerUsed[lane]) {
            activateLawnMower(lane);
        } else {
            gameOver = true;
            log(">>> The zombie ate your brain in lane " + lane + "! LOSER!!!");
        }
    }

    @Override
    public void log(String message) {
        System.out.println("  " + message);
    }

    // ── Demo helpers ──────────────────────────────────────────────────────────

    public void tickAdvance(int n) { currentTick += n; }
    public boolean isGameOver()    { return gameOver; }
    public int getCurrentTick()    { return currentTick; }

    /** Unregisters zombies that died this batch from the engine. */
    public void cleanupDeadZombies() {
        Iterator<Zombie> it = zombies.iterator();
        while (it.hasNext()) {
            Zombie z = it.next();
            if (z.isDead()) {
                engine.unRegister(z);
                it.remove();
            }
        }
    }

    // ── ASCII map rendering ───────────────────────────────────────────────────

    /**
     * Returns the full board as an ASCII string.
     *
     * <pre>
     * === Tick: 20 | Sun: 50 ===
     *        C0   C1   C2   C3   C4   C5   C6   C7   C8
     * [M] +----+----+----+----+----+----+----+----+----+
     *     |    |    |    |    |    |    |    |    |    |  Lane 0
     * [M] +----+----+----+----+----+----+----+----+----+
     *     |    |    |    |    |    |    | Z0 |    |    |  Lane 2
     * ...
     * </pre>
     */
    public String renderMap() {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb);
        appendColumnHeaders(sb);
        appendDivider(sb);
        for (int lane = 0; lane < LANES; lane++) {
            appendLaneRow(sb, lane);
            appendDivider(sb);
        }
        appendDirectionHint(sb);
        appendZombieStatus(sb);
        return sb.toString();
    }

    // ── Private rendering helpers ─────────────────────────────────────────────

    private void appendHeader(StringBuilder sb) {
        sb.append("\n=== Tick: ").append(currentTick)
          .append(" | Sun: ").append(currentSun)
          .append(" | Active zombies: ").append(zombies.size())
          .append(" ===\n");
    }

    private void appendColumnHeaders(StringBuilder sb) {
        sb.append("        ");
        for (int c = 0; c < COLS; c++) {
            sb.append(String.format("  C%-2d ", c));
        }
        sb.append("\n");
    }

    private void appendDivider(StringBuilder sb) {
        sb.append("    +");
        for (int c = 0; c < COLS; c++) {
            sb.append("----+");
        }
        sb.append("\n");
    }

    private void appendLaneRow(StringBuilder sb, int lane) {
        sb.append(lawnMowerUsed[lane] ? MOWER_USED : MOWER_OK).append(" |");
        for (int col = 0; col < COLS; col++) {
            sb.append(getCellContent(col, lane)).append('|');
        }
        sb.append("  Lane ").append(lane).append("\n");
    }

    private void appendDirectionHint(StringBuilder sb) {
        sb.append("         ←←←←←←← zombies walk this direction\n");
    }

    private void appendZombieStatus(StringBuilder sb) {
        if (zombies.isEmpty()) { sb.append("\n(no zombies)\n"); return; }
        sb.append("\nZombies (").append(zombies.size()).append(" active):\n");
        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            if (!z.isDead()) {
                sb.append("  Z").append(i).append(" ").append(z.toInfoString()).append("\n");
            }
        }
    }

    /** Returns 4-char cell content for rendering, e.g. {@code " Z0 "} or {@code "    "}. */
    private String getCellContent(int col, int lane) {
        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            if (!z.isDead() && z.getLane() == lane && (int) z.getX() == col) {
                return String.format("Z%-3d", i);
            }
        }
        return CELL_EMPTY;
    }

    // ── Private logic ─────────────────────────────────────────────────────────

    private void activateLawnMower(int lane) {
        lawnMowerUsed[lane] = true;
        log(">>> LAWN MOWER in lane " + lane + " ACTIVATED!");
        List<String> killed = new ArrayList<>();
        for (Zombie z : zombies) {
            if (z.getLane() == lane && !z.isDead()) {
                z.takeDamage(Float.MAX_VALUE);
                killed.add(z.getSheet().getAlias());
            }
        }
        if (!killed.isEmpty()) {
            log("    Killed: " + String.join(", ", killed));
        }
    }
}
