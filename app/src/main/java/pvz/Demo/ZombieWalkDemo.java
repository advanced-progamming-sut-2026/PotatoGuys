package pvz.Demo;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieFactory;
import pvz.Models.Entities.Zombies.effects.EffectType;
import pvz.Models.Entities.Zombies.effects.StatusEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Self-contained, interactive CLI demo of the zombie engine.
 *
 * <p>Spawns one {@code ZombieMummyDefault} at column 8, lane 2. It walks left
 * at 0.185 cells/second (10 ticks = 1 second) until it hits the lawn mower.
 *
 * <h3>Supported commands</h3>
 * <pre>
 *   advance time -t &lt;n&gt; ticks   advance n game ticks
 *   &lt;n&gt;                         shorthand for above
 *   show map                    print the ASCII grid
 *   zombies info                show HP / armour / effects for each zombie
 *   cheat spawn-zombie -t &lt;alias&gt; -l &lt;col,lane&gt;
 *   cheat add -n &lt;amount&gt; sun
 *   release the nuke            kill every zombie instantly
 *   chill &lt;zombieIndex&gt;         apply CHILL to zombie (slows to 50%)
 *   help                        list commands
 *   quit
 * </pre>
 *
 * <p>Run this class directly; it is independent of the main game menus.
 */
public class ZombieWalkDemo {

    // 10 ticks per in-game second — matches Zombie.TICKS_PER_SECOND
    private static final int TICKS_PER_SECOND = 10;

    private final GameEngine engine = new GameEngine();
    private final List<Zombie> zombies = new ArrayList<>();
    private final DemoGameContext ctx = new DemoGameContext(zombies, engine);
    private final ZombieFactory factory = new ZombieFactory();

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        new ZombieWalkDemo().run();
    }

    private void run() {
        printBanner();
        spawnZombie("ZombieMummyDefault", 8.0f, 2);
        System.out.println(ctx.renderMap());

        try (Scanner scanner = new Scanner(System.in)) {
            while (!ctx.isGameOver()) {
                System.out.print("pvz> ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                dispatch(line);
                ctx.cleanupDeadZombies();
                if (zombies.isEmpty() && !ctx.isGameOver()) {
                    System.out.println("\n  Dear humanz, zis is not done yet; we will come back!");
                    break;
                }
            }
        }
    }

    // ── Command dispatcher ────────────────────────────────────────────────────

    private void dispatch(String line) {
        if (line.startsWith("advance time -t ")) {
            doAdvance(extractTailNumber(line, "advance time -t ", " ticks"));
        } else if (line.matches("\\d+")) {
            doAdvance(line);
        } else if ("show map".equals(line)) {
            System.out.println(ctx.renderMap());
        } else if ("zombies info".equals(line)) {
            doZombiesInfo();
        } else if (line.startsWith("cheat spawn-zombie")) {
            doCheatSpawn(line);
        } else if (line.startsWith("cheat add")) {
            doCheatAdd(line);
        } else if ("release the nuke".equals(line)) {
            doNuke();
        } else if (line.startsWith("chill ")) {
            doChill(line);
        } else if ("help".equals(line)) {
            printHelp();
        } else if ("quit".equals(line) || "q".equals(line)) {
            System.exit(0);
        } else {
            System.out.println("  Unknown command. Type 'help'.");
        }
    }

    // ── Command handlers ──────────────────────────────────────────────────────

    /** Advance the simulation by {@code numStr} ticks and redraw the map. */
    private void doAdvance(String numStr) {
        try {
            int n = Integer.parseInt(numStr.trim());
            if (n <= 0) { System.out.println("  Must advance at least 1 tick."); return; }
            engine.advanceTime(n);
            ctx.tickAdvance(n);
            System.out.println(ctx.renderMap());
        } catch (NumberFormatException e) {
            System.out.println("  Invalid tick count: " + numStr);
        }
    }

    private void doZombiesInfo() {
        if (zombies.isEmpty()) { System.out.println("  No active zombies."); return; }
        System.out.println("\n--- Zombies Info ---");
        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            if (!z.isDead()) {
                System.out.println("Z" + i + " " + z.toInfoString());
            }
        }
        System.out.println();
    }

    /** {@code cheat spawn-zombie -t <alias> -l <col,lane>} */
    private void doCheatSpawn(String line) {
        try {
            String alias = extractFlag(line, "-t");
            String loc   = extractFlag(line, "-l");
            String[] parts = loc.split(",");
            float col  = Float.parseFloat(parts[0].trim());
            int   lane = Integer.parseInt(parts[1].trim());
            spawnZombie(alias, col, lane);
            System.out.println("  Spawned " + alias + " at (" + col + "," + lane + ")");
            System.out.println(ctx.renderMap());
        } catch (Exception e) {
            System.out.println("  Usage: cheat spawn-zombie -t <alias> -l <col,lane>");
        }
    }

    /** {@code cheat add -n <amount> sun} */
    private void doCheatAdd(String line) {
        try {
            String nStr = extractFlag(line, "-n");
            int n = Integer.parseInt(nStr.trim());
            ctx.returnSun(n);
            System.out.println("  Added " + n + " sun. Total: " + ctx.getSunAmount());
        } catch (Exception e) {
            System.out.println("  Usage: cheat add -n <amount> sun");
        }
    }

    private void doNuke() {
        System.out.println("  *** NUKE LAUNCHED ***");
        for (Zombie z : new ArrayList<>(zombies)) {
            z.takeDamage(Float.MAX_VALUE);
        }
        ctx.cleanupDeadZombies();
        System.out.println(ctx.renderMap());
    }

    /** {@code chill <zombieIndex>} — apply a 5-second chill to zombie i */
    private void doChill(String line) {
        try {
            int idx = Integer.parseInt(line.substring("chill ".length()).trim());
            if (idx < 0 || idx >= zombies.size()) {
                System.out.println("  No zombie at index " + idx); return;
            }
            Zombie z = zombies.get(idx);
            z.applyEffect(new StatusEffect(EffectType.CHILL, 5 * TICKS_PER_SECOND));
            System.out.println("  Z" + idx + " is now CHILLED (50% speed) for 5 seconds.");
        } catch (NumberFormatException e) {
            System.out.println("  Usage: chill <zombieIndex>");
        }
    }

    // ── Spawn helper ──────────────────────────────────────────────────────────

    private void spawnZombie(String alias, float col, int lane) {
        try {
            Zombie z = factory.create(alias, col, lane, ctx, 0, 3);
            zombies.add(z);
            engine.register(z);
        } catch (IllegalArgumentException e) {
            System.out.println("  Unknown alias: " + alias);
        }
    }

    // ── Print helpers ─────────────────────────────────────────────────────────

    private void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║    PvZ CLI Demo  —  Zombie Walking Left Engine           ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  Grid : 9 cols × 5 lanes  (col 0 = house, col 8 = spawn) ║");
        System.out.println("║  Zombie: ZombieMummyDefault  |  Speed: 0.185 cells/s     ║");
        System.out.println("║  10 ticks = 1 second  |  Type 'help' for commands        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private void printHelp() {
        System.out.println();
        System.out.println("  Commands:");
        System.out.println("  advance time -t <n> ticks  — advance n ticks (10 = 1 second)");
        System.out.println("  <n>                         — shorthand for above");
        System.out.println("  show map                    — redraw ASCII grid");
        System.out.println("  zombies info                — HP / armour / effects list");
        System.out.println("  cheat spawn-zombie -t <alias> -l <col,lane>");
        System.out.println("  cheat add -n <amount> sun");
        System.out.println("  chill <zombieIndex>         — slow zombie to 50% speed for 5s");
        System.out.println("  release the nuke            — instant kill all zombies");
        System.out.println("  quit (or q)");
        System.out.println();
        System.out.println("  Useful zombie aliases:");
        System.out.println("    ZombieMummyDefault        basic mummy");
        System.out.println("    ZombieMummyArmor1Default  conehead");
        System.out.println("    ZombieMummyArmor2Default  buckethead");
        System.out.println("    ZombieDarkArmor3Default   knight (shoulder + crown)");
        System.out.println("    ZombieEgyptGargantuar     gargantuar (throws imp at 50% HP)");
        System.out.println("    ZombieRaDefault           Ra (steals sun)");
        System.out.println("    ZombieWizardDefault       wizard (transforms plants to cats)");
        System.out.println();
    }

    // ── String utilities ──────────────────────────────────────────────────────

    private static String extractFlag(String line, String flag) {
        int idx = line.indexOf(flag);
        if (idx < 0) throw new IllegalArgumentException("Flag not found: " + flag);
        String rest = line.substring(idx + flag.length()).trim();
        int end = rest.indexOf(' ');
        return end > 0 ? rest.substring(0, end) : rest;
    }

    private static String extractTailNumber(String line, String prefix, String suffix) {
        String s = line;
        if (s.startsWith(prefix)) s = s.substring(prefix.length());
        if (s.endsWith(suffix))   s = s.substring(0, s.length() - suffix.length());
        return s.trim();
    }
}
