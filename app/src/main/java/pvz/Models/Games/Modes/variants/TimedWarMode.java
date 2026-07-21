package pvz.Models.Games.Modes.variants;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Constants;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Levels.variants.TimedWarLevel;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.Modes.Capabilities.PlantPlacer;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;

/**
 * Timed War game mode implementation.
 * Tracks rolling window zombie kills individually and enforces a level timer.
 */
public class TimedWarMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;

    private final int TICKS_PER_SECOND = 10; // هر چند تیک معادل یک ثانیه است (با انجین خود هماهنگ کنید)
    private final int WINDOW_SECONDS = 8;    // بازه زمانی بررسی (۵ ثانیه)
    private final int TARGET_KILLS = 5;     // تعداد زامبی هدف برای برد
    private final int TOTAL_TIME_LIMIT_SECONDS = 60; // زمان کل مرحله (مثلاً ۱ دقیقه)

    private final List<Integer> killedZombieTicks = new ArrayList<>();
    private List<Zombie> lastTickZombies = new ArrayList<>();

    public TimedWarMode(Level level) {
        if (level instanceof TimedWarLevel timedWarLevel) {
            waves = timedWarLevel.getWaves();
        }
        currentWave = waves.getFirst();
        SetupLawnMowers();
    }

    @Override
    public void initMode(GameContext context) {
        context.log("⏱ TIMED WAR MODE STARTED ⏱");
        context.log("Objective: Kill " + TARGET_KILLS + " zombies within any " + WINDOW_SECONDS + "-second window!");
        lastTickZombies = new ArrayList<>(context.getZombies());
    }

    @Override
    public void updateMode(GameContext context) {
        int currentTick = context.getCurrentTick();

        List<Zombie> currentZombies = context.getZombies();
        for (Zombie oldZombie : lastTickZombies) {
            // اگر زامبی در تیک قبل بود ولی الان نیست، یعنی حذف شده
            if (!currentZombies.contains(oldZombie)) {
                // مطمئن می‌شویم زامبی از خانه رد نشده باشد (کشته شده باشد یا با ماشین چمن‌زنی له شده باشد)
                if (oldZombie.getX() > 0f) {
                    killedZombieTicks.add(currentTick); // ثبت زمان دقیق مرگ
                }
            }
        }
        lastTickZombies = new ArrayList<>(currentZombies);

        // ۲. پاکسازی زامبی‌های منقضی شده (هر زامبی جداگانه پس از ۵ ثانیه از لیست خارج می‌شود)
        int windowTicks = WINDOW_SECONDS * TICKS_PER_SECOND;
        killedZombieTicks.removeIf(deathTick -> (currentTick - deathTick) > windowTicks);

        // ۳. بررسی شرایط برد
        if (killedZombieTicks.size() >= TARGET_KILLS) {
            context.setGameOver(true);
            context.log(" VICTORY! You successfully killed " + TARGET_KILLS + " zombies in a " + WINDOW_SECONDS + " second window!");
            return;
        }

        // ۴. بررسی شرایط باخت (اتمام زمان کل مرحله)
        if (currentTick >= TOTAL_TIME_LIMIT_SECONDS * TICKS_PER_SECOND) {
            context.setGameOver(true);
            context.log(" GAME OVER! Time ran out. You failed to reach the target kill streak.");
            return;
        }

        // ۵. مدیریت موج‌ها و لاجیک‌های پیش‌فرض بازی
        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                // در این مود تا زمان تمام نشود بازی ادامه دارد تا بازیکن شانس برد داشته باشد
                if (currentTick < TOTAL_TIME_LIMIT_SECONDS * TICKS_PER_SECOND) {
                    // زامبی‌های کمکی اسپان کنید یا منتظر اتمام تایمر بمانید
                }
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context);
        }

        // مدیریت ماشین‌های چمن‌زنی
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

        // مدیریت خورشیدها
        for (int i = 0; i < context.getSuns().size(); i++) {
            Sun sun = context.getSuns().get(i);
            if (sun.isDone()) {
                context.removeSun(sun);
                i--;
            }
        }
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) return false;
        if (!context.getPlantsAt(col, lane).isEmpty()) return false;
        if (card == null || !card.canUse()) return false;
        
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        return context.getCurrentSun() >= sheet.getSunCost();
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        if (!context.spendSun(sheet.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(card.getPlant().getType(), col, lane,
                card.getPlant().getLevel(), card.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane);
        card.use();
        context.log(card.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
    }

    @Override
    public PlantCard findCard(GameContext context, String plantType) {
        for (Card card : context.getCards()) {
            PlantCard plantCard = (PlantCard) card;
            if (plantCard.getPlant().getType().toString().equalsIgnoreCase(plantType)) {
                return plantCard;
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
            result.append("=== SEED PACKETS ===");
            int cardWidth = 40;
            for (Card card : cards) {
                PlantCard ps = (PlantCard) card;
                String cardInfo = String.format("- %s | Cost:%d | Lvl:%d | Cooldown:%.1f%s",
                        ps.getPlant().getType(), ps.getCost(), ps.getPlant().getLevel(),
                        (float)ps.getCooldown() / (float)Constants.TICK_PER_SECOND, ps.getPlant().isBoosted() ? " | ⚡B" : "");
                result.append("\n").append(String.format("%-" + cardWidth + "s", cardInfo));
            }
        }
        return result.toString();
    }

    private void SetupLawnMowers() {
        lawnMower = new Boolean[5];
        for (int i = 0; i < 5; i++) lawnMower[i] = false;
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane]) return;
        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE);
            context.removeZombie(zombie);
        });
        context.log("Lawn mower in lane " + lane + " ran over zombies!");
        lawnMower[lane] = true;
    }

    // ── بخش رندر نقشه با اطلاعات پنجره ۵ ثانیه‌ای لغزان ───────────────────

    private static final String CELL_EMPTY = "    ";
    private static final String MOWER_OK = "[M]";
    private static final String MOWER_USED = "[!]";

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();
        appendTimedWarHeader(sb, context); // هدر اختصاصی مود زماندار
        appendColumnHeaders(sb, context);
        appendDivider(sb, context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            appendLaneRow(sb, context, lane);
            appendDivider(sb, context);
        }
        return sb.toString();
    }

    private void appendTimedWarHeader(StringBuilder sb, GameContext context) {
        double totalTimeRemaining = Math.max(0, TOTAL_TIME_LIMIT_SECONDS - ((double) context.getCurrentTick() / TICKS_PER_SECOND));
        int currentKillsInWindow = killedZombieTicks.size();

        sb.append("\n=================================================================================\n");
        sb.append(String.format("  TIMED WAR MODE  |  Time Remaining: %.1f seconds\n", totalTimeRemaining));
        sb.append(String.format("  OBJECTIVE: Kill %d zombies within a %d-second rolling window\n", TARGET_KILLS, WINDOW_SECONDS));
        sb.append(String.format("  CURRENT STREAK (Last %d seconds): %d / %d zombies\n", WINDOW_SECONDS, currentKillsInWindow, TARGET_KILLS));
        sb.append("=================================================================================\n");
        
        // اطلاعات استاندارد تیک‌ها
        sb.append("Tick: ").append(context.getCurrentTick())
                .append(" | Sun: ").append(context.getCurrentSun())
                .append(" | Zombies Alive: ").append(context.getZombies().size())
                .append(" | Plants: ").append(context.getPlants().size())
                .append("\n");
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
        List<Plant> plantsAtCell = context.getPlantsAt(col, lane);
        boolean hasPlant = !plantsAtCell.isEmpty();

        List<Zombie> zombiesAtCell = context.getZombiesAt(col, lane);
        boolean hasZombie = !zombiesAtCell.isEmpty();

        if (hasPlant && hasZombie) {
            return String.format("P/Z%-1d", plantsAtCell.size());
        } else if (hasPlant) {
            return " P  ";
        } else if (hasZombie) {
            return String.format(" Z%-2d", zombiesAtCell.size());
        }
        return CELL_EMPTY;
    }
}