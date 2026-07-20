package pvz.Models.Games.Modes.variants;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Levels.variants.PlantWhatYouGetLevel;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;

public class PlantWhatYouGetMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;
    
    // فاز آمادگی در ابتدا فعال است
    private boolean preparationPhase = true; 

    public PlantWhatYouGetMode(Level level) {
        if (level instanceof PlantWhatYouGetLevel pwygLevel) {
            this.waves = pwygLevel.getWaves();
        }
        if (waves != null && !waves.isEmpty()) {
            currentWave = waves.get(0);
        }
        SetupLawnMowers();
    }

    @Override
    public void initMode(GameContext context) {
        context.log("🌱 Plant What You Get Mode Started! 🌱");
        context.log("Preparation Phase: You can plant freely without cooldowns.");
        context.log("Type 'start zombie waves' when you are ready!");
    }

    /**
     * متدی برای شروع هجوم زامبی‌ها (توسط کنترلر صدا زده می‌شود)
     */
    public void startZombieWaves(GameContext context) {
        if (!preparationPhase) {
            context.log("Zombie waves have already started!");
            return;
        }
        this.preparationPhase = false;
        context.log("🧟 The zombies are coming! Defense mode activated! 🧟");
    }

    public boolean isPreparationPhase() {
        return preparationPhase;
    }

    @Override
    public void updateMode(GameContext context) {
        // ۱. قطع باران آفتاب: خورشیدهایی که از آسمان می‌بارند را فوراً حذف می‌کنیم
        for (int i = 0; i < context.getSuns().size(); i++) {
            Sun sun = context.getSuns().get(i);
            context.removeSun(sun);
            i--;
        }

        // ۲. اگر در فاز آمادگی باشیم، زامبی‌ها جلو نمی‌آیند و موج‌ها آپدیت نمی‌شوند
        if (preparationPhase) {
            return; 
        }

        // ۳. مدیریت موج زامبی‌ها پس از اتمام فاز آمادگی
        if (currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("Congratulations! You survived with your limited sun resources!");
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context);
        }

        // ۴. منطق ماشین‌های چمن‌زنی و باخت بازی
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
                    context.log("Zombies ate your brains!");
                    context.removeZombie(z);
                }
            }
        }
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) return false;
        if (!context.getPlantsAt(col, lane).isEmpty()) return false;
        if (!(card instanceof PlantCard plantCard)) return false;

        // ممنوعیت انتخاب/کاشت گل آفتابگردان
        String plantName = plantCard.getPlant().getType().toString();
        if (plantName.equalsIgnoreCase("SUNFLOWER") || plantName.contains("SUN")) {
            context.log("You cannot use sun-producing plants in this mode!");
            return false;
        }

        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        if (!(card instanceof PlantCard plantCard)) return;

        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");

        // اگر در فاز آمادگی باشیم، بلافاصله کول‌داون کارت را ریست می‌کنیم تا بازیکن منتظر نماند
        if (preparationPhase) {
            // اگر در کلاس PlantCard متدی برای ریست زمان شارژ داری اینجا صدا بزن، مثلاً:
            // plantCard.resetCooldown(); 
            context.log("Preparation bonus: Cooldown skipped!");
        }
    }

    @Override
    public String getCardsStatus(GameContext context) {
        StringBuilder sb = new StringBuilder("=== Available Cards ===\n");
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc) {
                String status = preparationPhase ? "READY (Prep)" : "Standard";
                sb.append(String.format("- %s | Cost: %d | Status: %s\n", 
                        pc.getPlant().getType(), pc.getCost(), status));
            }
        }
        return sb.toString().trim();
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

    @Override
    public String renderMap(GameContext context) {
        // می‌توانی از همان منطق رندر ساده قبلی استفاده کنی
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== TICK: ").append(context.getCurrentTick())
          .append(" | SUN: ").append(context.getCurrentSun())
          .append(preparationPhase ? " | [PREPARATION PHASE]" : " | [WAVES ACTIVE]")
          .append(" ===\n");
        return sb.toString();
    }
}