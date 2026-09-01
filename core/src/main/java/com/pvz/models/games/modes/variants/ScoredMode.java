package com.pvz.models.games.modes.variants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.effects.ChapterEffect;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.ScoredLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;

/**
 * Scored ("mini-point") game mode. Plays exactly like the normal adventure mode
 * (waves of zombies, lawn defense, win/loss), but the meaningful output is the
 * number of <b>miopoints</b> the player racks up before the game ends.
 *
 * <p>
 * Miopoints are awarded through 5 distinct scoring patterns, so that different
 * strategies yield different scores:
 * <ol>
 *   <li><b>MULTI_KILL</b> - 3+ zombies taken out in a single burst (an
 *       explosive / area blast counts as "many zombies with one hit").</li>
 *   <li><b>SPEED_KILL</b> - a zombie dies very shortly after it spawned.</li>
 *   <li><b>SIMULTANEOUS_KILL</b> - 2+ zombies die in the exact same tick.</li>
 *   <li><b>COMBO_CHAIN</b> - uninterrupted kills (no long pause) build an
 *       escalating combo multiplier.</li>
 *   <li><b>STREAK_BONUS</b> - reaching kill-count milestones grants a bonus.</li>
 * </ol>
 *
 * <p>
 * Every award is queued as a {@link ScoredEvent}. The controller drains
 * {@link #drainEvents()} each frame and raises a pop-up ("You scored N
 * points!") so the player gets immediate feedback.
 *
 * <p>
 * The zombie generation is driven by the level's JSON waves, exactly like the
 * normal mode, so every player faces the same pattern. The final miopoint total
 * is stored on the user record and surfaced through the leaderboard / "My
 * Point" profile section.
 */
public class ScoredMode implements GameMode, PlantPlacer {

    /** A single scoring-award event destined for the UI pop-up. */
    public static class ScoredEvent {
        public final int points;

        public ScoredEvent(int points) {
            this.points = points;
        }
    }

    // Scoring-tunable constants.
    private static final float SPEED_KILL_SECONDS = 3.0f;   // die within 3s of spawn
    private static final float MULTI_KILL_WINDOW = 0.20f;   // 3+ deaths in one burst
    private static final int   MULTI_KILL_MIN = 3;
    private static final int   MULTI_KILL_PER_KILL = 100;
    private static final int   SPEED_KILL_POINTS = 50;
    private static final int   SIMULTANEOUS_PER_KILL = 75;
    private static final float COMBO_WINDOW = 2.5f;         // reset combo if idle
    private static final int   COMBO_MAX = 20;
    private static final int   COMBO_PER_LEVEL = 25;
    private static final int   STREAK_STEP = 5;             // milestone every 5 kills
    private static final int   STREAK_BONUS_BASE = 100;

    private Wave currentWave;
    private List<Wave> waves;

    private float stateTime;
    private int totalMiopoints;

    private final Map<Zombie, Float> zombieSpawnTime = new HashMap<>();
    private final List<Zombie> lastTickAlive = new ArrayList<>();
    private final Deque<Float> recentDeaths = new ArrayDeque<>();

    private int combo;
    private float lastKillTime;
    private int killCount;
    private int lastStreakMilestone;

    private final Deque<ScoredEvent> pendingEvents = new ArrayDeque<>();

    public ScoredMode(Level level) {
        if (level instanceof ScoredLevel scoredLevel) {
            waves = scoredLevel.getWaves();
        } else if (level != null) {
            waves = level.getWaves();
        }
        if (waves != null && !waves.isEmpty()) {
            currentWave = waves.getFirst();
        }
    }

    public int getTotalMiopoints() {
        return totalMiopoints;
    }

    public int getKillCount() {
        return killCount;
    }

    /** Drains any pending scoring awards so the UI can display pop-ups. */
    public List<ScoredEvent> drainEvents() {
        if (pendingEvents.isEmpty()) {
            return List.of();
        }
        List<ScoredEvent> out = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return out;
    }

    // GameMode lifecycle.

    @Override
    public boolean hasProgressBar() {
        return true;
    }

    @Override
    public int getCurrentWaveIndex() {
        return waves.indexOf(currentWave);
    }

    @Override
    public int getTotalWaves() {
        return waves.size();
    }

    @Override
    public int getTotalZombieCount() {
        return waves.stream().mapToInt(Wave::getTotalZombieCount).sum();
    }

    @Override
    public void initMode(GameContext context) {
        stateTime = 0f;
        totalMiopoints = 0;
        combo = 0;
        killCount = 0;
        lastKillTime = Float.NEGATIVE_INFINITY;
        lastStreakMilestone = 0;
        pendingEvents.clear();
        recentDeaths.clear();
        zombieSpawnTime.clear();
        lastTickAlive.clear();
        lastTickAlive.addAll(alive(context.getZombies()));
        if (currentWave != null) {
            currentWave.startWave(context);
            context.getGameStats().onFirstWaveStart(0);
            for (ChapterEffect effect : context.getActiveEffects()) {
                effect.onWaveStart(currentWave, context);
            }
        }
        context.log("[Scored] Mini-point mode started - every kill can earn miopoints!");
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        stateTime += dt;

        // 1) Wave progression / win condition (identical to NormalMode).
        if (currentWave != null && currentWave.isDone() && context.getZombies().isEmpty()) {
            int nextWaveIndex = waves.indexOf(currentWave) + 1;
            if (nextWaveIndex < waves.size()) {
                currentWave = waves.get(nextWaveIndex);
                currentWave.startWave(context);
                for (ChapterEffect effect : context.getActiveEffects()) {
                    effect.onWaveStart(currentWave, context);
                }
                context.log("Wave " + currentWave.getWaveNumber() + " started.");
            } else {
                context.setGameOver(true);
                context.log("[Scored] All waves cleared. Final miopoints: " + totalMiopoints);
            }
            return;
        }

        if (currentWave != null && !currentWave.isDone()) {
            currentWave.updateWave(context, dt);
        }

        detectKillsAndScore(context);

        // 2) Loss condition - a zombie reached the house.
        for (int i = 0; i < context.getZombies().size(); i++) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= GameController.colToWorldX(-1)) {
                context.setGameOver(true);
                context.log("[Scored] A zombie reached your house. Final miopoints: " + totalMiopoints);
                context.removeZombie(z);
            }
        }

        // 3) Sun cleanup.
        for (Sun sun : new ArrayList<>(context.getSuns())) {
            if (sun.isDone()) {
                context.removeSun(sun);
            }
        }
    }

    // Scoring engine.

    private List<Zombie> alive(List<Zombie> zombies) {
        List<Zombie> out = new ArrayList<>();
        for (Zombie z : zombies) {
            if (!z.isDead()) {
                out.add(z);
            }
        }
        return out;
    }

    private void detectKillsAndScore(GameContext context) {
        List<Zombie> current = context.getZombies();

        // Record spawn times for any zombie that just appeared.
        for (Zombie z : current) {
            if (!z.isDead() && !zombieSpawnTime.containsKey(z)) {
                zombieSpawnTime.put(z, stateTime);
            }
        }

        // Collect the zombies that were alive last tick and are dead now.
        List<Zombie> newlyDead = new ArrayList<>();
        for (Zombie z : lastTickAlive) {
            if (z.isDead()) {
                newlyDead.add(z);
            }
        }

        if (!newlyDead.isEmpty()) {
            handleKills(context, newlyDead);
        }

        lastTickAlive.clear();
        lastTickAlive.addAll(alive(current));

        // Drop zombie records that are no longer present.
        zombieSpawnTime.keySet().removeIf(z -> !current.contains(z));
    }

    private void handleKills(GameContext context, List<Zombie> kills) {
        killCount += kills.size();
        recentDeaths.addLast(stateTime);
        updateCombo(kills.size());

        // Pattern 1: Simultaneous kill - 2+ zombies died in the very same tick.
        if (kills.size() >= 2) {
            int points = kills.size() * SIMULTANEOUS_PER_KILL;
            award(context, points);
            context.log("[Scored] SIMULTANEOUS_KILL (" + kills.size() + " at once) +" + points);
        }

        // Pattern 2: Multi-kill - 3+ zombies felled in one rapid burst ("one shot").
        int burst = countKillsInWindow(MULTI_KILL_WINDOW);
        if (burst >= MULTI_KILL_MIN) {
            int points = burst * MULTI_KILL_PER_KILL;
            award(context, points);
            context.log("[Scored] MULTI_KILL (" + burst + " in one burst) +" + points);
        }

        // Pattern 3: Speed-kill - each zombie that died shortly after spawning.
        for (Zombie z : kills) {
            Float spawnT = zombieSpawnTime.get(z);
            float age = (spawnT == null) ? stateTime : stateTime - spawnT;
            if (age <= SPEED_KILL_SECONDS) {
                award(context, SPEED_KILL_POINTS);
                context.log("[Scored] SPEED_KILL +" + SPEED_KILL_POINTS);
            }
        }

        // Pattern 4: Combo chain - escalating multiplier for uninterrupted kills.
        if (combo >= 2) {
            int points = combo * COMBO_PER_LEVEL;
            award(context, points);
            context.log("[Scored] COMBO_CHAIN x" + combo + " +" + points);
        }

        // Pattern 5: Streak bonus - kill-count milestones.
        int milestone = killCount / STREAK_STEP;
        if (milestone > lastStreakMilestone) {
            lastStreakMilestone = milestone;
            int points = milestone * STREAK_BONUS_BASE;
            award(context, points);
            context.log("[Scored] STREAK_BONUS (" + (milestone * STREAK_STEP) + " kills) +" + points);
        }

        // Prune death times older than the multi-kill window.
        float cutoff = stateTime - MULTI_KILL_WINDOW;
        while (!recentDeaths.isEmpty() && recentDeaths.peekFirst() < cutoff) {
            recentDeaths.removeFirst();
        }
        lastKillTime = stateTime;
    }

    private int countKillsInWindow(float window) {
        int n = 0;
        float cutoff = stateTime - window;
        for (float t : recentDeaths) {
            if (t >= cutoff) {
                n++;
            }
        }
        return n;
    }

    private void updateCombo(int killsThisTick) {
        if (stateTime - lastKillTime <= COMBO_WINDOW && combo > 0) {
            combo += killsThisTick;
        } else {
            combo = killsThisTick;
        }
        if (combo > COMBO_MAX) {
            combo = COMBO_MAX;
        }
    }

    private void award(GameContext context, int points) {
        totalMiopoints += points;
        pendingEvents.addLast(new ScoredEvent(points));
        context.log("[Scored] +" + points + " miopoints (total " + totalMiopoints + ")");
    }

    // PlantPlacer capability (identical to NormalMode).

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
        if (!context.getTileAt(col, lane).isPlantable(card)) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") does not support planting "
                    + card.getPlant().getType());
            return false;
        }
        if (!card.canUse()) {
            context.log("[Placement Failed] Card " + card.getPlant().getType() + " is on cooldown or locked.");
            return false;
        }
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (context.getCurrentSun() < stats.getSunCost()) {
            context.log("[Placement Failed] Not enough sun for " + sheet.getName()
                    + "! Required: " + stats.getSunCost() + ", Current: " + context.getCurrentSun());
            return false;
        }
        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        if (!(card instanceof PlantCard plantCard)) {
            context.log("Error: card is not a plant card.");
            return;
        }
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (!context.spendSun(stats.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
        if (!plant.getAttackAction().isPlantableOnTile(context.getTileAt(col, lane))) {
            return;
        }
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane, plantCard.getPlant().getType());
        plantCard.use();
        context.log(plantCard.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
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
}
