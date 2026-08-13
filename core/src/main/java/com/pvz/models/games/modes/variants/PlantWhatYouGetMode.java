package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;

import com.pvz.controller.game.GameController;
import com.pvz.models.Constants;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.PlantWhatYouGetLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.games.modes.capabilities.StartWaves;

public class PlantWhatYouGetMode implements GameMode, PlantPlacer, StartWaves {
    private Wave currentWave;
    private List<Wave> waves;

    // فاز آمادگی در ابتدا فعال است
    private boolean preparationPhase = true;

    public PlantWhatYouGetMode(Level level) {
        if (level instanceof PlantWhatYouGetLevel pwygLevel) {
            this.waves = pwygLevel.getWaves();
        }
        if (waves != null && !waves.isEmpty()) {
            currentWave = waves.get(0);
        }
    }

    @Override
    public void initMode(GameContext context) {
        context.log("\n=========================================================");
        context.log("🌱 PLANT WHAT YOU GET MODE ACTIVATED! 🌱");
        context.log("• Strategy: You only have your initial Sun resource.");
        context.log("• Rule: No sky suns! No Sunflowers allowed!");
        context.log("• Phase: PREPARATION! Plant freely without cooldowns.");
        context.log("• Command: Type 'start zombie waves' when your defense is ready!");
        context.log("=========================================================\n");
    }

    /**
     * متدی برای شروع هجوم زامبی‌ها (توسط کنترلر صدا زده می‌شود)
     */
    @Override
    public void startZombieWaves(GameContext context) {
        if (!preparationPhase) {
            context.log("Zombie waves have already started!");
            return;
        }
        this.preparationPhase = false;
        context.log("\n🧟 The zombies are coming! Defense mode activated! Cooldowns are now active! 🧟\n");

        // شروع اولین موج زامبی‌ها
        if (currentWave != null) {
            currentWave.startWave(context);
        }
    }

    @Override
    public boolean isPreparationPhase() {
        return preparationPhase;
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        // ۱. قطع باران آفتاب: هر خورشیدی که در محیط بازی ساخته شود (از آسمان تولید شود)
        // فوراً حذف می‌گردد
        for (Sun sun : new ArrayList<>(context.getSuns())) {
            context.removeSun(sun);
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
                context.log("🎉 CONGRATULATIONS! You survived the onslaught with your limited sun resources! 🎉");
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context, 0);
        }

        // ۴. منطق ماشین‌های چمن‌زنی و باخت بازی
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= 0f) {
                context.setGameOver(true);
                context.log("❌ GAME OVER! The zombies ate your brains! ❌");
                context.removeZombie(z);

            }
        }
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        if (card == null) {
            context.log("[Placement Failed] Selected card is null.");
            return false;
        }

        if (col < 0 || col >= context.getMap().getColumns() || lane < 0 || lane >= context.getMap().getLanes()) {
            context.log("[Placement Failed] Out of bounds: (" + col + ", " + lane + ")");
            return false;
        }

        if (!context.getPlantsAt(col, lane).isEmpty()) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") is already occupied by another plant.");
            return false;
        }

        if (!context.getTileAt(col, lane).isPlantable(card)) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") does not support planting "
                        + card.getPlant().getType());
            return false;
        }

        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());

        if (context.getCurrentSun() < stats.getSunCost()) {
            context.log("[Placement Failed] Not enough sun for " + sheet.getName()
                        + "! Required: " + stats.getSunCost() + ", Current: " + context.getCurrentSun());
            return false;
        }

        // During preparation phase, cooldowns are bypassed so the player can plant freely.
        if (!preparationPhase && !card.canUse()) {
            context.log("[Placement Failed] Card " + card.getPlant().getType() + " is on cooldown.");
            return false;
        }

        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        if (!(card instanceof PlantCard plantCard))
            return;

        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (!context.spendSun(stats.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }

        // ساخت و اسپان گیاه
        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane, plantCard.getPlant().getType());

        // مدیریت زمان شارژ (Recharge / Cooldown)
        if (preparationPhase) {
            // در فاز آمادگی، کارت مصرف نمی‌شود و به کول‌داون نمی‌رود تا بازیکن بدون وقفه
            // بکارد
            context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane
                    + ") [PREP BONUS: No Cooldown].");
        } else {
            // در فاز اصلی، کارت وارد Cooldown می‌شود
            plantCard.use();
            context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
        }
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
    }

    @Override
    public String getCardsStatus(GameContext context) {
        StringBuilder sb = new StringBuilder();
        List<Card> cards = context.getCards();

        if (cards == null || cards.isEmpty()) {
            sb.append("No plant cards available.");
        } else {
            sb.append("=== SEED PACKETS ===");
            int cardWidth = 45;

            for (Card card : cards) {
                PlantCard ps = (PlantCard) card;
                String cooldownStatus = preparationPhase ? "0.0 (PREP FREE)"
                        : String.format("%.1f", (float) ps.getCooldown() / (float) Constants.TICK_PER_SECOND);

                String cardInfo = String.format("- %s | Cost:%d | Lvl:%d | CD:%s%s",
                        ps.getPlant().getType(),
                        ps.getCost(),
                        ps.getPlant().getLevel(),
                        cooldownStatus,
                        ps.getPlant().isBoosted() ? " | ⚡B" : "");

                sb.append("\n");
                sb.append(String.format("%-" + cardWidth + "s", cardInfo));
            }
        }
        return sb.toString();
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
}
