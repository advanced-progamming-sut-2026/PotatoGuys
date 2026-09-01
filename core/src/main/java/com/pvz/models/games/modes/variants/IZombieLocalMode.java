package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.sun.SunType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.entities.zombies.fsm.VisualEatState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.variants.IZombieLocalLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.GameModeType;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.games.modes.capabilities.ZombiePlacer;
import com.pvz.models.user.MyPlant;

/**
 * Local two-player split I,Zombie mode.
 *
 * <p>
 * Both sides share one screen: the plant player uses the mouse and spends the
 * normal {@link GameContext} sun pool; the zombie player uses the keyboard and
 * spends a separate {@link #zombieSun} pool. Unlike {@link IZombieMode} there
 * is no networking — both controlled here, so the zombie sun economy is
 * tracked directly in this class.
 */
public class IZombieLocalMode implements GameMode, ZombiePlacer, PlantPlacer {
    private static final float PLANT_SURVIVAL_SECONDS = 120f;

    private final List<MyPlant> basedPlants;
    private final List<ZombieType> basedZombies;
    private boolean[] brainsEaten;
    private int laneCount = 5;
    private int redLineColumn = 6;

    private boolean sunProducersSpawned = false;
    private final Map<Zombie, Float> brainEatTimers = new java.util.IdentityHashMap<>();
    private static final float BRAIN_EAT_DURATION = 3.0f;

    private static final String SUN_PRODUCER_ALIAS = "ZombieTutorialArmor2Default";
    private static final int SUN_PRODUCER_HP = 1290;
    private static final int SUN_AMOUNT = 25;
    private static final float SUN_PRODUCTION_INTERVAL_SECONDS = 10f;
    private final List<Zombie> sunProducers = new ArrayList<>();
    private final java.util.IdentityHashMap<Zombie, Float> sunProducerTimers = new java.util.IdentityHashMap<>();

    /** The zombie (keyboard) player's separate sun pool. */
    private int zombieSun = 0;

    /** Real-time (dt-based) countdown; plants win at zero. */
    private float plantSurvivalSecondsRemaining = PLANT_SURVIVAL_SECONDS;

    private boolean zombieSideStuck = false;

    public enum Outcome {
        IN_PROGRESS, ZOMBIES_WIN, PLANTS_WIN
    }

    private static final Map<ZombieType, Integer> ZOMBIE_SUN_COSTS = new HashMap<>();
    static {
        ZOMBIE_SUN_COSTS.put(ZombieType.IMP, 25);
        ZOMBIE_SUN_COSTS.put(ZombieType.BASIC, 50);
        ZOMBIE_SUN_COSTS.put(ZombieType.CONEHEAD, 75);
        ZOMBIE_SUN_COSTS.put(ZombieType.BUCKETHEAD, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.BRICKHEAD, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.NEWSPAPER, 75);
        ZOMBIE_SUN_COSTS.put(ZombieType.FLAG, 50);
        ZOMBIE_SUN_COSTS.put(ZombieType.GARGANTUAR, 300);
        ZOMBIE_SUN_COSTS.put(ZombieType.MUMMY, 50);
        ZOMBIE_SUN_COSTS.put(ZombieType.MUMMY_CONE, 75);
        ZOMBIE_SUN_COSTS.put(ZombieType.MUMMY_BUCKET, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.MUMMY_BRICK, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.RA, 150);
        ZOMBIE_SUN_COSTS.put(ZombieType.EXPLORER, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.TOMB_RAISER, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.EGYPT_GARG, 300);
        ZOMBIE_SUN_COSTS.put(ZombieType.EGYPT_IMP, 25);
        ZOMBIE_SUN_COSTS.put(ZombieType.ICE_AGE, 50);
        ZOMBIE_SUN_COSTS.put(ZombieType.ICE_AGE_CONE, 75);
        ZOMBIE_SUN_COSTS.put(ZombieType.ICE_AGE_BUCKET, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.HUNTER, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.TROGLOBITE, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.DODO, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.ICE_AGE_GARG, 300);
        ZOMBIE_SUN_COSTS.put(ZombieType.ICE_AGE_IMP, 25);
        ZOMBIE_SUN_COSTS.put(ZombieType.BEACH, 50);
        ZOMBIE_SUN_COSTS.put(ZombieType.BEACH_CONE, 75);
        ZOMBIE_SUN_COSTS.put(ZombieType.BEACH_BUCKET, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.SNORKEL, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.FISHERMAN, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.OCTOPUS, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.BEACH_GARG, 300);
        ZOMBIE_SUN_COSTS.put(ZombieType.BEACH_IMP, 25);
        ZOMBIE_SUN_COSTS.put(ZombieType.DARK, 50);
        ZOMBIE_SUN_COSTS.put(ZombieType.DARK_CONE, 75);
        ZOMBIE_SUN_COSTS.put(ZombieType.DARK_BUCKET, 100);
        ZOMBIE_SUN_COSTS.put(ZombieType.KNIGHT, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.DARK_BRICK, 150);
        ZOMBIE_SUN_COSTS.put(ZombieType.WIZARD, 150);
        ZOMBIE_SUN_COSTS.put(ZombieType.JUGGLER, 125);
        ZOMBIE_SUN_COSTS.put(ZombieType.KING, 200);
        ZOMBIE_SUN_COSTS.put(ZombieType.DARK_GARG, 300);
        ZOMBIE_SUN_COSTS.put(ZombieType.DARK_IMP, 25);
    }

    public IZombieLocalMode(Level level) {
        if (level instanceof IZombieLocalLevel izLevel) {
            this.basedPlants = izLevel.getBasedPlants();
            this.basedZombies = izLevel.getBasedZombies();
            this.redLineColumn = izLevel.getRedLineColumn();
            if (izLevel.getGameMode() == GameModeType.SPLIT_IZOMBIE) {
                this.zombieSun = izLevel.getZombieSun();
            }
        } else {
            throw new IllegalArgumentException("Level must be an instance of IZombieLocalLevel.");
        }
        this.brainsEaten = new boolean[laneCount];
    }

    public boolean[] getBrainsEaten() {
        return brainsEaten;
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    public List<Zombie> getSunProducers() {
        return sunProducers;
    }

    public float getPlantSurvivalSecondsRemaining() {
        return plantSurvivalSecondsRemaining;
    }

    public void setPlantSurvivalSecondsRemaining(float seconds) {
        this.plantSurvivalSecondsRemaining = seconds;
    }

    /** Returns the zombie (keyboard) player's sun pool. */
    public int getZombieSun() {
        return zombieSun;
    }

    public void addZombieSun(int amount) {
        zombieSun += amount;
    }

    public boolean spendZombieSun(int amount) {
        if (zombieSun >= amount) {
            zombieSun -= amount;
            return true;
        }
        return false;
    }

    public Outcome getOutcome() {
        boolean allBrainsEaten = true;
        for (int i = 0; i < laneCount; i++) {
            if (!brainsEaten[i]) {
                allBrainsEaten = false;
                break;
            }
        }
        if (allBrainsEaten)
            return Outcome.ZOMBIES_WIN;
        if (plantSurvivalSecondsRemaining <= 0f)
            return Outcome.PLANTS_WIN;
        if (zombieSideStuck)
            return Outcome.PLANTS_WIN;
        return Outcome.IN_PROGRESS;
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
    }

    @Override
    public void initMode(GameContext context) {
        laneCount = context.getMap().getLanes();
        brainsEaten = new boolean[laneCount];
        setupZombieCards(context);
        setupPlantCards(context);
        context.log("Split I,Zombie ready! " + basedZombies.size() + " zombie types available, "
                + (basedPlants == null ? 0 : basedPlants.size()) + " plant types available.");
    }

    private void setupZombieCards(GameContext context) {
        if (basedZombies == null || basedZombies.isEmpty())
            return;
        for (ZombieType zt : basedZombies) {
            if (zt == null)
                continue;
            int cost = ZOMBIE_SUN_COSTS.getOrDefault(zt, 50);
            float cooldown = Math.max(3f, Math.min(12f, 2f + cost / 50f));
            ZombieCard card = new ZombieCard(zt, cost, cooldown);
            card.setCooldownEnable(false);
            context.addCard(card);
        }
    }

    private void setupPlantCards(GameContext context) {
        if (basedPlants == null || basedPlants.isEmpty())
            return;
        for (MyPlant mp : basedPlants) {
            if (mp == null)
                continue;
            PlantPropertySheet sheet = com.pvz.models.entities.plants.config.PlantConfigRegistry.getInstance()
                    .resolveSheet(mp.getType());
            if (sheet == null)
                continue;
            ResolvedStats stats = PlantStatResolver.resolve(sheet, mp.getLevel());
            int sunCost = (stats != null) ? stats.getSunCost() : sheet.getSunCost();
            float recharge = (stats != null && stats.getRechargeSeconds() != null)
                    ? stats.getRechargeSeconds()
                    : (sheet.getRechargeSeconds() != null ? sheet.getRechargeSeconds() : 5f);
            context.addCard(new PlantCard(mp, sunCost, recharge));
        }
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        if (!sunProducersSpawned) {
            sunProducersSpawned = true;
            int lastCol = context.getMap().getColumns() - 1;
            float spawnX = GameController.colToWorldX(lastCol);
            int lanes = context.getMap().getLanes();
            for (int lane = 0; lane < lanes; lane++) {
                try {
                    Zombie producer = new ZombieFactory().create(SUN_PRODUCER_ALIAS, spawnX, lane, context, 1, 1);
                    producer.setPendingInitialState(new com.pvz.models.entities.zombies.fsm.IdleState());
                    producer.setHp(SUN_PRODUCER_HP);
                    context.spawnZombie(producer);
                    sunProducers.add(producer);
                    sunProducerTimers.put(producer, SUN_PRODUCTION_INTERVAL_SECONDS);
                } catch (Exception e) {
                    Gdx.app.error("IZombieLocalMode", "Failed to spawn sun producer: " + e.getMessage());
                }
            }
        }

        float brainX = GameController.colToWorldX(-1);
        int lanes = context.getMap().getLanes();
        List<Zombie> toRemove = new ArrayList<>();
        for (Zombie z : context.getZombies()) {
            if (z.isDead() || sunProducers.contains(z))
                continue;
            if (z.getX() <= brainX + 10f) {
                int lane = GameController.worldYtoLane(z.getY());
                if (lane >= 0 && lane < lanes && lane < brainsEaten.length) {
                    if (!brainsEaten[lane]) {
                        if (!brainEatTimers.containsKey(z)) {
                            brainEatTimers.put(z, BRAIN_EAT_DURATION);
                            z.changeState(new VisualEatState());
                        }
                        z.setX(brainX);
                        float remaining = brainEatTimers.get(z) - dt;
                        brainEatTimers.put(z, remaining);
                        if (remaining <= 0) {
                            brainsEaten[lane] = true;
                            toRemove.add(z);
                        }
                    } else {
                        toRemove.add(z);
                    }
                } else {
                    toRemove.add(z);
                }
            }
        }
        for (Zombie z : toRemove) {
            brainEatTimers.remove(z);
            context.removeZombie(z);
        }

        boolean allBrainsEaten = true;
        for (int i = 0; i < lanes; i++) {
            if (!brainsEaten[i]) {
                allBrainsEaten = false;
                break;
            }
        }
        if (allBrainsEaten) {
            context.setGameOver(true);
            return;
        }

        plantSurvivalSecondsRemaining -= dt;
        if (plantSurvivalSecondsRemaining <= 0f) {
            plantSurvivalSecondsRemaining = 0f;
            context.setGameOver(true);
            return;
        }

        java.util.Iterator<java.util.Map.Entry<Zombie, Float>> timerIt = sunProducerTimers.entrySet().iterator();
        while (timerIt.hasNext()) {
            java.util.Map.Entry<Zombie, Float> entry = timerIt.next();
            Zombie z = entry.getKey();
            if (z.isDead()) {
                timerIt.remove();
                continue;
            }
            float remaining = entry.getValue() - dt;
            if (remaining <= 0) {
                int col = GameController.worldXtoCol(z.getX());
                int lane = GameController.worldYtoLane(z.getY());
                Sun sun = new Sun(SunType.NORMAL, col, lane, SUN_AMOUNT, false, context);
                sun.setOwner(Sun.SunOwner.ZOMBIE);
                sun.getCurrentPos().add(0, 30f);
                context.spawnSun(sun);
                remaining = SUN_PRODUCTION_INTERVAL_SECONDS;
            }
            entry.setValue(remaining);
        }

        boolean hasLivingPlayerZombies = false;
        boolean hasLivingSunProducers = false;
        for (Zombie z : context.getZombies()) {
            if (z.isDead())
                continue;
            if (sunProducers.contains(z)) {
                hasLivingSunProducers = true;
            } else {
                hasLivingPlayerZombies = true;
            }
        }
        if (!hasLivingPlayerZombies) {
            int cheapest = Integer.MAX_VALUE;
            for (Card card : context.getCards()) {
                if (card instanceof ZombieCard zc && zombieSun >= zc.getCost()) {
                    cheapest = Math.min(cheapest, zc.getCost());
                }
            }
            if (cheapest == Integer.MAX_VALUE && !hasLivingSunProducers) {
                zombieSideStuck = true;
                context.setGameOver(true);
            }
        }
    }

    // ---- ZombiePlacer ----

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        if (card == null || !(card instanceof ZombieCard zombieCard))
            return false;
        if (col < redLineColumn || col >= context.getMap().getColumns())
            return false;
        if (lane < 0 || lane >= context.getMap().getLanes())
            return false;
        if (!zombieCard.canUse())
            return false;
        if (zombieSun < zombieCard.getCost())
            return false;
        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (!(card instanceof ZombieCard zombieCard))
            return;
        if (!spendZombieSun(zombieCard.getCost())) {
            Gdx.app.log("IZombieLocalMode", "Not enough zombie sun for " + zombieCard.getZombieType());
            return;
        }
        try {
            float spawnX = GameController.colToWorldX(col);
            Zombie zombie = new ZombieFactory().create(
                    zombieCard.getZombieType().getAlias(),
                    spawnX, lane, context, 1, 1);
            context.spawnZombie(zombie);
            zombieCard.use();
        } catch (Exception e) {
            Gdx.app.error("IZombieLocalMode", "Failed to place " + zombieCard.getZombieType() + ": " + e.getMessage());
            addZombieSun(zombieCard.getCost());
        }
    }

    @Override
    public ZombieCard findZombieCard(GameContext context, String zombieType) {
        for (Card card : context.getCards()) {
            if (card instanceof ZombieCard zc && zc.getZombieType().toString().equalsIgnoreCase(zombieType)) {
                return zc;
            }
        }
        return null;
    }

    // ---- PlantPlacer ----

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        if (card == null) {
            context.log("[Placement Failed] Selected card is null.");
            return false;
        }
        if (col < 0 || col >= redLineColumn) {
            context.log("[Placement Failed] Plants can only be placed "
                    + "left of the red line (col < " + redLineColumn + ").");
            return false;
        }
        if (lane < 0 || lane >= context.getMap().getLanes()) {
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
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (!context.spendSun(stats.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(card.getPlant().getType(), col, lane,
                card.getPlant().getLevel(), card.getPlant().isBoosted(), context);
        if (plant == null) {
            context.addSun(stats.getSunCost());
            return;
        }
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane, card.getPlant().getType());
        card.use();
        context.log(card.getPlant().getType() + " placed at (" + col + ", " + lane + ").");
    }

    @Override
    public PlantCard findCard(GameContext context, String plantType) {
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard plantCard
                    && plantCard.getPlant().getType().toString().equalsIgnoreCase(plantType)) {
                return plantCard;
            }
        }
        return null;
    }
}
