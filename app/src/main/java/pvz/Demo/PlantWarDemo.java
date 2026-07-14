package pvz.Demo;

import java.util.List;
import java.util.Scanner;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;

/**
 * Self-contained CLI demo exercising the full Plants + Projectiles + Suns
 * system end-to-end, alongside the existing Zombie engine — i.e. the
 * "SYSTEM FLOW" required by the project brief: {@link GameEngine#advanceTime}
 * drives every {@link pvz.Models.Engine.TickAware} entity (plants,
 * projectiles, suns, zombies) through one shared tick loop.
 *
 * <h3>Commands</h3>
 * <pre>
 *   plant &lt;Type&gt; &lt;col&gt; &lt;lane&gt;        plant e.g. "plant Peashooter 3 2"
 *   feed &lt;col&gt; &lt;lane&gt;                trigger that plant's Plant Food effect
 *   collect &lt;col&gt; &lt;lane&gt;             collect a sun sitting at that cell
 *   cheat spawn-zombie -t &lt;alias&gt; -l &lt;col,lane&gt;
 *   cheat add -n &lt;amount&gt; sun
 *   advance &lt;n&gt;                       advance n ticks (10 = 1 second)
 *   show map                          redraw the ASCII grid
 *   plants info / zombies info
 *   help / quit
 * </pre>
 */
public class PlantWarDemo {

    private final GameEngine engine = new GameEngine();
    private final PlantWarContext ctx = new PlantWarContext(engine, 200);
    private final PlantFactory plantFactory = new PlantFactory();
    private int currentTick;

    public static void main(String[] args) {
        new PlantWarDemo().run();
    }

    private void run() {
        printBanner();
        renderMap();

        try (Scanner scanner = new Scanner(System.in)) {
            while (!ctx.isGameOver()) {
                System.out.print("pvz> ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                dispatch(line);
            }
        }
    }

    private void dispatch(String line) {
        String[] parts = line.split("\\s+");
        try {
            switch (parts[0].toLowerCase()) {
                case "plant" -> doPlant(parts);
                case "feed" -> doFeed(parts);
                case "collect" -> doCollect(parts);
                case "advance" -> doAdvance(parts);
                case "sun" -> System.out.println("  Sun: " + ctx.getSunAmount());
                case "show" -> renderMap();
                case "plants" -> doPlantsInfo();
                case "zombies" -> doZombiesInfo();
                case "cheat" -> doCheat(line, parts);
                case "help" -> printHelp();
                case "quit", "q" -> System.exit(0);
                default -> System.out.println("  Unknown command. Type 'help'.");
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── Command handlers ──────────────────────────────────────────────────────

    private void doPlant(String[] parts) {
        PlantType type = PlantType.valueOf(parts[1]);
        int col = Integer.parseInt(parts[2]);
        int lane = Integer.parseInt(parts[3]);
        if (ctx.isPlantAt(col, lane)) { System.out.println("  Cell already occupied."); return; }

        Plant plant = plantFactory.create(type, col, lane, ctx);
        if (!ctx.trySpendSun(plant.getSunCost())) {
            System.out.println("  Not enough sun (" + plant.getSunCost() + " needed, have "
                    + ctx.getSunAmount() + ").");
            return;
        }
        ctx.addPlant(plant);
        System.out.println("  Planted " + type + " at (" + col + "," + lane + ") for " + plant.getSunCost() + " sun.");
        renderMap();
    }

    private void doFeed(String[] parts) {
        int col = Integer.parseInt(parts[1]);
        int lane = Integer.parseInt(parts[2]);
        Plant p = ctx.findPlant(col, lane);
        if (p == null) { System.out.println("  No plant there."); return; }
        p.triggerPlantFood(ctx);
    }

    private void doCollect(String[] parts) {
        int col = Integer.parseInt(parts[1]);
        int lane = Integer.parseInt(parts[2]);
        if (ctx.collectSunAt(col, lane)) {
            System.out.println("  Collected sun. Total: " + ctx.getSunAmount());
        } else {
            System.out.println("  No sun there.");
        }
    }

    private void doAdvance(String[] parts) {
        int n = Integer.parseInt(parts[1]);
        engine.advanceTime(n);
        currentTick += n;
        ctx.cleanup();
        renderMap();
    }

    private void doCheat(String line, String[] parts) {
        if (line.startsWith("cheat spawn-zombie")) {
            String alias = extractFlag(line, "-t");
            String loc = extractFlag(line, "-l");
            String[] xy = loc.split(",");
            ctx.spawnZombie(alias, Integer.parseInt(xy[0].trim()), Integer.parseInt(xy[1].trim()));
            renderMap();
        } else if (line.startsWith("cheat add")) {
            int n = Integer.parseInt(extractFlag(line, "-n"));
            ctx.addSun(n);
            System.out.println("  Added " + n + " sun. Total: " + ctx.getSunAmount());
        } else {
            System.out.println("  Unknown cheat.");
        }
    }

    private void doPlantsInfo() {
        List<Plant> plants = ctx.getPlants();
        if (plants.isEmpty()) { System.out.println("  No plants on the board."); return; }
        System.out.println("\n--- Plants Info ---");
        for (Plant p : plants) if (!p.isDead()) System.out.println(p.toInfoString());
    }

    private void doZombiesInfo() {
        List<Zombie> zombies = ctx.getZombies();
        if (zombies.isEmpty()) { System.out.println("  No active zombies."); return; }
        System.out.println("\n--- Zombies Info ---");
        for (Zombie z : zombies) if (!z.isDead()) System.out.println(z.toInfoString());
    }

    // ── Rendering ──────────────────────────────────────────────────────────────

    private void renderMap() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Tick: ").append(currentTick).append(" | Sun: ").append(ctx.getSunAmount())
          .append(" | Plants: ").append(ctx.getPlants().size())
          .append(" | Zombies: ").append(ctx.getZombies().size())
          .append(" | Projectiles: ").append(ctx.getProjectiles().size())
          .append(" | Suns: ").append(ctx.getSuns().size()).append(" ===\n");
        sb.append("        ");
        for (int c = 0; c < PlantWarContext.COLS; c++) sb.append(String.format("  C%-2d ", c));
        sb.append("\n");
        for (int lane = 0; lane < PlantWarContext.LANES; lane++) {
            appendDivider(sb);
            sb.append("    |");
            for (int col = 0; col < PlantWarContext.COLS; col++) {
                sb.append(cell(col, lane)).append('|');
            }
            sb.append("  Lane ").append(lane).append("\n");
        }
        appendDivider(sb);
        System.out.println(sb);
    }

    private void appendDivider(StringBuilder sb) {
        sb.append("    +");
        for (int c = 0; c < PlantWarContext.COLS; c++) sb.append("----+");
        sb.append("\n");
    }

    private String cell(int col, int lane) {
        Plant p = ctx.findPlant(col, lane);
        Zombie z = null;
        for (Zombie candidate : ctx.getZombies()) {
            if (!candidate.isDead() && candidate.getLane() == lane && (int) candidate.getX() == col) {
                z = candidate;
                break;
            }
        }
        if (p != null && z != null) return "P+Z";
        if (z != null) return String.format("Z%-3s", "");
        if (p != null) return String.format("%-4s", p.getSheet().getName().substring(0, Math.min(3, p.getSheet().getName().length())));
        for (Sun s : ctx.getSuns()) {
            if (!s.isDone() && s.getCol() == col && s.getLane() == lane) return " $  ";
        }
        return "    ";
    }

    private static String extractFlag(String line, String flag) {
        int idx = line.indexOf(flag);
        if (idx < 0) throw new IllegalArgumentException("Flag not found: " + flag);
        String rest = line.substring(idx + flag.length()).trim();
        int end = rest.indexOf(' ');
        return end > 0 ? rest.substring(0, end) : rest;
    }

    private void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   PvZ CLI Demo — Plants + Projectiles + Suns + Zombies   ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  Grid : 9 cols × 5 lanes  (col 0 = house, col 8 = spawn) ║");
        System.out.println("║  Starting sun: 200 | 10 ticks = 1 second                 ║");
        System.out.println("║  Type 'help' for commands                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    private void printHelp() {
        System.out.println();
        System.out.println("  plant <Type> <col> <lane>   e.g. plant Peashooter 3 2");
        System.out.println("  feed <col> <lane>            trigger that plant's Plant Food effect");
        System.out.println("  collect <col> <lane>         collect a sun at that cell");
        System.out.println("  cheat spawn-zombie -t <alias> -l <col,lane>");
        System.out.println("  cheat add -n <amount> sun");
        System.out.println("  advance <n>                  advance n ticks (10 = 1 second)");
        System.out.println("  show map / plants info / zombies info");
        System.out.println("  quit (or q)");
        System.out.println();
    }
}
