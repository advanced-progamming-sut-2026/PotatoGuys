package pvz.models.games.modes.variants;

import java.util.List;

import pvz.models.Constants;
import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.PlantFactory;
import pvz.models.entities.sun.Sun;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;
import pvz.models.games.card.Card;
import pvz.models.games.card.PlantCard;
import pvz.models.games.levels.Level;
import pvz.models.games.levels.Wave;
import pvz.models.games.levels.variants.DeadLineLevel;
import pvz.models.games.modes.GameMode;
import pvz.models.games.modes.capabilities.PlantPlacer;

public class DeadLineMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;
    private int deadlineColumn; // زامبی از این ستون بگذرد بازیکن می‌بازد

    public DeadLineMode(Level level) {
        if (level instanceof DeadLineLevel deadLineLevel) {
            this.waves = deadLineLevel.getWaves();
            this.deadlineColumn = deadLineLevel.getDeadlineColumn();
        }
        if (waves != null && !waves.isEmpty()) {
            currentWave = waves.get(0);
        }
        SetupLawnMowers();
    }

    @Override
    public void initMode(GameContext context) {
        context.log("⚠️ DEADLINE MODE! Don't let zombies cross Column " + deadlineColumn + " ⚠️");
    }

    @Override
    public void updateMode(GameContext context) {
        // ─── ۱. بررسی شرط باخت فوری (عبور از ددلاین) ──────────────────────────
        for (Zombie z : context.getZombies()) {
            // زامبی‌ها از راست به چپ می‌آیند؛ پس اگر X کمتر یا مساوی خط ددلاین شد یعنی عبور کرده‌اند
            if (!z.isDead() && z.getX() <= deadlineColumn) {
                context.setGameOver(true);
                context.log("💥 GAME OVER! A zombie crossed the Dead Line at Column " + deadlineColumn + "!");
                return;
            }
        }

        // ─── ۲. مدیریت موج‌ها ────────────────────────────────────────────────
        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Zombies defeated! You defended the Dead Line successfully!");
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context);
        }

        // ─── ۳. ماشین‌های چمن زنی (اگر ددلاین جلوتر باشد عملا این بخش برای زامبی‌های عبوری اجرا نمی‌شود) ───
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
                    context.log("Brain has eaten");
                    context.removeZombie(z);
                }
            }
        }

        // ─── ۴. پاکسازی خورشیدها ─────────────────────────────────────────────
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
        return card instanceof PlantCard;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        if (!(card instanceof PlantCard plantCard)) return;

        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
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
        
            for (int i = 0; i < cards.size(); i++) {
                PlantCard ps = (PlantCard) cards.get(i);
                
                String cardInfo = String.format("- %s | Cost:%d | Lvl:%d | Cooldown:%.1f%s",
                        ps.getPlant().getType(),
                        ps.getCost(),
                        ps.getPlant().getLevel(),
                        (float)ps.getCooldown() / (float)Constants.TICK_PER_SECOND,
                        ps.getPlant().isBoosted() ? " | ⚡B" : "" 
                );
            
                result.append("\n");
                result.append(String.format("%-" + cardWidth + "s", cardInfo));
            }
        }
        return result.toString();
    }

    @Override
    public PlantCard findCard(GameContext context, String plantType) {
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard plantCard) {
                if (plantCard.getPlant().getType().toString().equalsIgnoreCase(plantType)) {
                    return plantCard;
                }
            }
        }
        return null;
    }

    private void SetupLawnMowers() {
        lawnMower = new Boolean[5];
        for (int i = 0; i < 5; i++) lawnMower[i] = false;
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane]) return;
        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE, true);
            context.removeZombie(zombie);
        });
        lawnMower[lane] = true;
    }

    // =========================================================================
    // ─── رندر اختصاصی نقشه همراه با خط ددلاین ──────────────────────────────────
    // =========================================================================

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== TICK: ").append(context.getCurrentTick()).append(" | DEADLINE AT COLUMN: ").append(deadlineColumn).append(" ===\n");
        
        sb.append("\n     ");
        for (int c = 0; c < context.getColumns(); c++) sb.append(String.format(" C%-2d ", c));
        sb.append("\n");

        appendDivider(sb, context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            sb.append(lawnMower[lane] ? "[!]" : "[M]").append(" |");
            for (int col = 0; col < context.getColumns(); col++) {
                sb.append(getCellContent(col, context, lane));
                
                if (col == deadlineColumn - 1) {
                    sb.append("║");
                } else {
                    sb.append("|");
                }
            }
            sb.append("  Lane ").append(lane).append("\n");
            appendDivider(sb, context);
        }
        return sb.toString();
    }

    private void appendDivider(StringBuilder sb, GameContext context) {
        sb.append("    +");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append("----+");
        }
        sb.append("\n");
    }

    private String getCellContent(int col, GameContext context, int lane) {
        var plants = context.getPlantsAt(col, lane);
        var zombies = context.getZombiesAt(col, lane);
        if (!plants.isEmpty() && !zombies.isEmpty()) return "P/Z ";
        if (!plants.isEmpty()) return " P  ";
        if (!zombies.isEmpty()) return String.format(" Z%-2d", zombies.size());
        return "    ";
    }
}