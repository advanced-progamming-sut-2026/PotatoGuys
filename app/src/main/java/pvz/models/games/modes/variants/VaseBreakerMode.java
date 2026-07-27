package pvz.models.games.modes.variants;

import java.util.List;
import java.util.Random;

import pvz.models.Constants;
import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.PlantFactory;
import pvz.models.entities.zombies.Zombie;
import pvz.models.entities.zombies.ZombieFactory;
import pvz.models.entities.zombies.ZombieType;
import pvz.models.games.GameContext;
import pvz.models.games.levels.Level;
import pvz.models.games.levels.data.VaseDefinition;
import pvz.models.games.levels.data.VaseType;
import pvz.models.games.levels.variants.VaseBreakerLevel;
import pvz.models.games.modes.GameMode;
import pvz.models.games.modes.capabilities.PlantPlacer;
import pvz.models.games.modes.capabilities.VaseBreaker;
import pvz.models.games.card.Card;
import pvz.models.games.card.PlantCard;
import pvz.models.user.MyPlant;

public class VaseBreakerMode implements GameMode, VaseBreaker, PlantPlacer {

    private static class VaseTile {
        VaseType type;
        boolean isBroken;

        VaseTile(VaseType type) {
            this.type = type;
            this.isBroken = false;
        }
    }

    private final VaseTile[][] vaseGrid;
    private final List<MyPlant> plantPool;
    private final List<ZombieType> zombiePool;
    private final Boolean[] lawnMower;
    private final Random random = new Random();

    public VaseBreakerMode(Level level) {
        int rows = level.getGameMapDefinition().rows;
        int cols = level.getGameMapDefinition().columns;
        this.vaseGrid = new VaseTile[rows][cols];

        if (level instanceof VaseBreakerLevel vaseLevel) {
            this.plantPool = vaseLevel.getBasedPlants();
            this.zombiePool = vaseLevel.getBasedZombies();

            if (vaseLevel.getVases() != null) {
                for (VaseDefinition def : vaseLevel.getVases()) {
                    if (def.getLane() >= 0 && def.getLane() < rows && def.getCol() >= 0 && def.getCol() < cols) {
                        this.vaseGrid[def.getLane()][def.getCol()] = new VaseTile(def.getVaseType());
                    }
                }
            }
        } else {
            this.plantPool = List.of();
            this.zombiePool = List.of();
        }

        this.lawnMower = new Boolean[rows];
        for (int i = 0; i < rows; i++) {
            this.lawnMower[i] = false;
        }
    }

    @Override
    public void initMode(GameContext context) {
        context.log("Vasebreaker level started! Break vases to find plants or zombies.");
    }

    @Override
    public void updateMode(GameContext context) {
        // شرط پایان بازی و پیروزی
        if (!anyVasesRemain() && context.getZombies().isEmpty()) {
            context.setGameOver(true);
            context.log("VICTORY! All vases cleared and all zombies defeated!");
            return;
        }

        // بررسی ورود زامبی‌ها به انتهای خانه و چمن‌زن
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= 0f) {
                if (!lawnMower[z.getLane()]) {
                    runLawnMowers(context, z.getLane());
                    i--;
                    continue;
                }
                if (lawnMower[z.getLane()]) {
                    context.setGameOver(true);
                    context.log("The zombie ate your brain; LOOSER!!!");
                    context.removeZombie(z);
                }
            }
        }

        for (int i = 0; i < context.getCards().size(); i++) {
            Card card = context.getCards().get(i);
            if (card.getCooldown() <= 0.01f) {
                context.removeCard(card);
                i--;
            }
        }
    }

    @Override
    public void breakVase(GameContext context, int col, int lane) {
        if (lane < 0 || lane >= vaseGrid.length || col < 0 || col >= vaseGrid[0].length) {
            context.log("Error: Target tile (" + col + ", " + lane + ") is out of bounds.");
            return;
        }

        VaseTile vase = vaseGrid[lane][col];
        if (vase == null || vase.isBroken) {
            context.log("There is no intact vase at (" + col + ", " + lane + ").");
            return;
        }

        vase.isBroken = true;
        context.log("Vase at (" + col + ", " + lane + ") shattered!");

        spawnRandomContent(context, vase.type, col, lane);
    }

    private void spawnRandomContent(GameContext context, VaseType vaseType, int col, int lane) {
        if (vaseType == VaseType.GARGANTUAR) {
            spawnZombie(context, ZombieType.GARGANTUAR, col, lane);
            return;
        }

        if (vaseType == VaseType.PLANT) {
            spawnRandomPlantCard(context);
            return;
        }

        // کوزه معمولی (NORMAL): تعیین محتوا بر اساس درصد شانس
        double roll = random.nextDouble();
        if (roll < 0.45) { // ۴۵٪ شانس زامبی
            if (!zombiePool.isEmpty()) {
                ZombieType randomZombie = zombiePool.get(random.nextInt(zombiePool.size()));
                spawnZombie(context, randomZombie, col, lane);
            }
        } else if (roll < 0.85) { // ۴۰٪ شانس کارت گیاه
            spawnRandomPlantCard(context);
        } else if (roll < 0.95) { // ۱۰٪ شانس خورشید
            context.addSun(50);
            context.log("Collected 50 Sun from the vase!");
        } else { // ۵٪ پوچ
            context.log("The vase was empty!");
        }
    }

    private void spawnZombie(GameContext context, ZombieType type, int col, int lane) {
        Zombie zombie = new ZombieFactory().create(type.getAlias(), col, lane, context, 0, 0);
        if (zombie != null) {
            context.spawnZombie(zombie);
            context.log("ALERT: " + type + " emerged from the vase at (" + col + ", " + lane + ")!");
        }
    }

    private void spawnRandomPlantCard(GameContext context) {
        if (!plantPool.isEmpty()) {
            MyPlant myPlant = plantPool.get(random.nextInt(plantPool.size()));
            PlantCard card = new PlantCard(myPlant, 0, 6);
            context.addCard(card);
            context.log("REWARD: Received " + myPlant.getType() + " seed packet!");
        }
    }

    @Override
    public void showVases(GameContext context) {
        context.log("=== VASES ON BOARD ===");
        for (int r = 0; r < vaseGrid.length; r++) {
            for (int c = 0; c < vaseGrid[0].length; c++) {
                if (vaseGrid[r][c] != null && !vaseGrid[r][c].isBroken) {
                    context.log(String.format("Vase at (%d, %d) -> Type: %s", c, r, vaseGrid[r][c].type));
                }
            }
        }
    }

    private boolean anyVasesRemain() {
        for (VaseTile[] row : vaseGrid) {
            for (VaseTile tile : row) {
                if (tile != null && !tile.isBroken)
                    return true;
            }
        }
        return false;
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane])
            return;
        context.getZombiesInLane(lane).forEach(z -> {
            z.takeDamage(Float.MAX_VALUE);
            context.removeZombie(z);
            context.log("Lawn mower in lane " + lane + " ran over a zombie!");
        });
        lawnMower[lane] = true;
    }

    // ── قابلیت PlantPlacer ───────────────────────────────────────────────────

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        if (card == null) {
            context.log("[Placement Failed] Selected card is null.");
            return false;
        }

        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
            context.log("[Placement Failed] Out of bounds: (" + col + ", " + lane + ")");
            return false;
        }

        if (!context.getPlantsAt(col, lane).isEmpty()) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") is already occupied by another plant.");
            return false;
        }

        if (vaseGrid[lane][col] != null && !vaseGrid[lane][col].isBroken) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") has an intact vase. Break it first!");
            return false;
        }

        if (!context.getTileAt(col, lane).isPlantable(card)) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") does not support planting "
                        + card.getPlant().getType());
            return false;
        }

        if (!card.canUse()) {
            context.log("[Placement Failed] Card " + card.getPlant().getType() + " is on cooldown or locked.");
            return false;
        }

        // VaseBreakerMode: Cards are earned by breaking vases and are free; no sun cost check needed.
        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        Plant plant = new PlantFactory().create(card.getPlant().getType(), col, lane,
                card.getPlant().getLevel(), card.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        card.use();
        context.removeCard(card); // کارت‌های کوزه‌شکنی تک‌بار مصرف هستند
        context.log(card.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
    }

    @Override
    public PlantCard findCard(GameContext context, String plantType) {
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc && pc.getPlant().getType().toString().equalsIgnoreCase(plantType)) {
                return pc;
            }
        }
        return null;
    }

    @Override
    public String getCardsStatus(GameContext context) {
        StringBuilder result = new StringBuilder();
        List<Card> cards = context.getCards();
        if (cards == null || cards.isEmpty()) {
            result.append("No plant cards available.");
        } else {
            result.append("=== HELD SEED PACKETS ===");
            for (Card card : cards) {
                if (card instanceof PlantCard ps) {
                    result.append(String.format("\n- %s | Lvl:%d | Expires in:%.1fs", ps.getPlant().getType(),
                            ps.getPlant().getLevel(), (float) ps.getCooldown() / (float) Constants.TICK_PER_SECOND));
                }
            }
        }
        return result.toString();
    }

    // ── رندر کردن دقیق نقشه (Map Rendering) ──────────────────────────────────

    private static final String CELL_EMPTY = "    ";
    private static final String MOWER_OK = "[M]";
    private static final String MOWER_USED = "[!]";

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb, context);
        appendColumnHeaders(sb, context);
        appendDivider(sb, context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            appendLaneRow(sb, context, lane);
            appendDivider(sb, context);
        }
        return sb.toString();
    }

    private void appendHeader(StringBuilder sb, GameContext context) {
        int remainingVases = 0;
        for (VaseTile[] row : vaseGrid) {
            for (VaseTile v : row) {
                if (v != null && !v.isBroken)
                    remainingVases++;
            }
        }

        sb.append("\n=== Tick: ").append(context.getCurrentTick())
                .append(" | Sun: ").append(context.getCurrentSun())
                .append(" | Vases Remaining: ").append(remainingVases)
                .append(" | Zombies: ").append(context.getZombies().size())
                .append(" | Plants: ").append(context.getPlants().size())
                .append(" ===\n");
    }

    private void appendColumnHeaders(StringBuilder sb, GameContext context) {
        sb.append("\n     ");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append(String.format(" C%-2d ", c));
        }
        sb.append("\n");
    }

    private void appendDivider(StringBuilder sb, GameContext context) {
        sb.append("    +");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append("----+");
        }
        sb.append("\n");
    }

    private void appendLaneRow(StringBuilder sb, GameContext context, int lane) {
        sb.append(lawnMower[lane] ? MOWER_USED : MOWER_OK).append(" |");
        for (int col = 0; col < context.getColumns(); col++) {
            sb.append(getCellContent(col, context, lane)).append('|');
        }
        sb.append("  Lane ").append(lane).append("\n");
    }

    private String getCellContent(int col, GameContext context, int lane) {
        // ۱. اگر کوزه نشکسته در این خانه باشد، نماد کوزه را رسم کن
        VaseTile vase = vaseGrid[lane][col];
        if (vase != null && !vase.isBroken) {
            return switch (vase.type) {
                case PLANT -> " P? ";
                case GARGANTUAR -> " G? ";
                default -> " ?? ";
            };
        }

        // ۲. اگر کوزه شکسته یا وجود نداشت، وضعیت گیاه‌/زامبی روی خانه را رسم کن
        List<Plant> plantsAtCell = context.getPlantsAt(col, lane);
        boolean hasPlant = !plantsAtCell.isEmpty();

        List<Zombie> zombiesAtCell = context.getZombiesAt(col, lane);
        boolean hasZombie = !zombiesAtCell.isEmpty();

        if (hasPlant && hasZombie) {
            return String.format("P/Z%-1d", zombiesAtCell.size());
        } else if (hasPlant) {
            return " P  ";
        } else if (hasZombie) {
            return String.format(" Z%-2d", zombiesAtCell.size());
        }

        return CELL_EMPTY;
    }
}