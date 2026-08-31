package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.pvz.controller.game.GameController;
import com.pvz.models.Constants;
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
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.variants.IZombieLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.games.modes.capabilities.ZombiePlacer;
import com.pvz.models.user.MyPlant;

/**
 * I, Zombie: one player defends with plants (PlantPlacer, same
 * card-and-cooldown
 * mechanics as NormalMode, restricted to the columns left of the red line),
 * the other attacks with zombies (ZombiePlacer, unchanged from before).
 *
 * Networked play (Phase 3): the plant-side client is always the authoritative
 * host — it's the one running GameEngine, so it's the only side whose
 * PlantCard cooldowns actually tick. The zombie-side client is the guest; see
 * GameController and GameStateSync for how its input/rendering are wired.
 * This class itself doesn't know or care whether it's networked — it's driven
 * identically either way, exactly like single-player.
 */
public class IZombieMode implements GameMode, ZombiePlacer, PlantPlacer {
    private static final float PLANT_SURVIVAL_SECONDS = 120f;

    private final List<MyPlant> basedPlants;
    private final List<ZombieType> basedZombies;
    private boolean[] brainsEaten;
    private int laneCount = 5;
    private int redLineColumn = 6;

    private int ticksElapsed = 0;
    private boolean sunProducersSpawned = false;
    private boolean guestSunProducersSpawned = false;
    private final java.util.Map<Zombie, Float> brainEatTimers = new java.util.IdentityHashMap<>();
    private static final float BRAIN_EAT_DURATION = 3.0f;

    private static final String SUN_PRODUCER_ALIAS = "ZombieTutorialArmor2Default";
    private static final int SUN_PRODUCER_HP = 1290;
    private static final int SUN_AMOUNT = 25;
    private static final float SUN_PRODUCTION_INTERVAL_SECONDS = 10f;
    private final List<Zombie> sunProducers = new ArrayList<>();
    private final java.util.IdentityHashMap<Zombie, Float> sunProducerTimers = new java.util.IdentityHashMap<>();
    private int zombieSun = 0;

    /**
     * Counts down from PLANT_SURVIVAL_SECONDS in real time (dt-based, not ticks).
     * Plants win if this hits zero before all five brains are eaten.
     */
    private float plantSurvivalSecondsRemaining = PLANT_SURVIVAL_SECONDS;

    /**
     * Set when the zombie side runs out of live zombies, sun producers, and
     * affordable cards all at once — mirrors what used to be an unconditional
     * loss; now (with a real plant-side player) it's a plant win.
     */
    private boolean zombieSideStuck = false;

    /**
     * Set by the guest in a networked match so that sun checks use
     * {@link #zombieSun}.
     */
    private boolean networkGuest = false;

    /**
     * Set by the host in a networked match so that zombie placement skips sun
     * spending
     * (the guest already spent zombie sun locally).
     */
    private boolean networkHost = false;

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

    public IZombieMode(Level level) {
        if (level instanceof IZombieLevel iZombieLevel) {
            this.basedPlants = iZombieLevel.getBasedPlants();
            this.basedZombies = iZombieLevel.getBasedZombies();
            this.redLineColumn = iZombieLevel.getRedLineColumn();
        } else {
            throw new IllegalArgumentException("Level must be an instance of IZombieLevel.");
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

    /**
     * Used only by the network layer (GameStateSync) on a guest client, to mark a
     * reconstructed zombie as a sun producer so the floating-sun-icon overlay
     * (GameController#drawIZombieOverlay) still works on the guest's own screen.
     */
    public void registerRemoteSunProducer(Zombie z) {
        if (!sunProducers.contains(z)) {
            sunProducers.add(z);
            sunProducerTimers.put(z, SUN_PRODUCTION_INTERVAL_SECONDS);
        }
    }

    /**
     * Guest (zombie) side: spawns the per-lane sun-producer zombies as
     * lightweight local entities. They are NOT rendered — the guest draws whatever
     * the host sends (the remote frame already includes the producers and their
     * sun icons) — but they exist locally so {@link #updateSunProducers} can mint
     * the player's own collectible zombie suns. Rendered state and actual gameplay
     * stay on the host.
     */
    public void ensureGuestSunProducers(GameContext context) {
        if (guestSunProducersSpawned)
            return;
        guestSunProducersSpawned = true;
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
                Gdx.app.error("IZombieMode", "Failed to spawn guest sun producer: " + e.getMessage());
            }
        }
    }

    public void setNetworkGuest(boolean networkGuest) {
        this.networkGuest = networkGuest;
    }

    public void setNetworkHost(boolean networkHost) {
        this.networkHost = networkHost;
    }

    public boolean isNetworkGuest() {
        return networkGuest;
    }

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

    /**
     * Who's currently ahead, from a neutral (not per-role) point of view.
     * GameController maps this onto "did *I* win" using the local player's role.
     */
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
        // The plant-side cards only belong on the plant-playing side (single-player
        // or the network host). A network guest plays zombies only, so skip them.
        if (!networkGuest) {
            setupPlantCards(context);
        }
        context.log("I, Zombie ready! " + basedZombies.size() + " zombie types available, "
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

    /**
     * Mirrors setupZombieCards: one PlantCard per allowed plant type, using the
     * same cost/recharge resolution NormalMode uses (real cooldown, unlike
     * zombie cards — the plant side plays a normal PvZ card economy).
     */
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
        ticksElapsed++;

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
                    Gdx.app.error("IZombieMode", "Failed to spawn sun producer: " + e.getMessage());
                }
            }
        }

        // Brain eating: brains sit at column -1 (off-screen, like lawn mowers).
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
                        // Brain not yet eaten — start (or continue) eating.
                        if (!brainEatTimers.containsKey(z)) {
                            brainEatTimers.put(z, BRAIN_EAT_DURATION);
                        }
                        // Pin zombie in place at the brain while it eats.
                        z.setX(brainX);
                        float remaining = brainEatTimers.get(z) - dt;
                        brainEatTimers.put(z, remaining);
                        if (remaining <= 0) {
                            brainsEaten[lane] = true;
                            toRemove.add(z);
                        }
                    } else {
                        // Brain already eaten in this lane — zombie walks past, remove it.
                        toRemove.add(z);
                    }
                } else {
                    // Out-of-range lane — just remove the zombie.
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

        // Plant-side win condition: survive PLANT_SURVIVAL_SECONDS without losing
        // all five brains. Real elapsed time, not a tick count.
        plantSurvivalSecondsRemaining -= dt;
        if (plantSurvivalSecondsRemaining <= 0f) {
            plantSurvivalSecondsRemaining = 0f;
            context.setGameOver(true);
            return;
        }

        // Per-zombie sun production (host side — needed for single-player IZombie;
        // filtered out of snapshots by GameStateSync so the guest never sees them)
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
                if (card instanceof ZombieCard zc && context.getCurrentSun() >= zc.getCost()) {
                    cheapest = Math.min(cheapest, zc.getCost());
                }
            }
            if (cheapest == Integer.MAX_VALUE && !hasLivingSunProducers) {
                zombieSideStuck = true;
                context.setGameOver(true);
            }
        }
    }

    // ---- ZombiePlacer
    // ------------------------------------------------------------------

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
        // Guest validates against its own zombie sun; host skips sun check
        // (the guest already validated before sending the action).
        if (networkHost)
            return true;
        int availableSun = networkGuest ? zombieSun : context.getCurrentSun();
        if (availableSun < zombieCard.getCost())
            return false;
        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (!(card instanceof ZombieCard zombieCard))
            return;
        if (networkGuest) {
            if (!spendZombieSun(zombieCard.getCost())) {
                Gdx.app.log("IZombieMode", "Not enough zombie sun for " + zombieCard.getZombieType());
                return;
            }
        } else if (!networkHost) {
            if (!context.spendSun(zombieCard.getCost())) {
                Gdx.app.log("IZombieMode", "Not enough sun for " + zombieCard.getZombieType());
                return;
            }
        }
        // else networkHost: guest already spent zombie sun, no local deduction needed
        try {
            float spawnX = GameController.colToWorldX(col);
            Zombie zombie = new ZombieFactory().create(zombieCard.getZombieType().getAlias(), spawnX, lane, context, 1,
                    1);
            context.spawnZombie(zombie);
            zombieCard.use();
        } catch (Exception e) {
            Gdx.app.error("IZombieMode", "Failed to place " + zombieCard.getZombieType() + ": " + e.getMessage());
            if (networkGuest) {
                addZombieSun(zombieCard.getCost());
            } else {
                context.addSun(zombieCard.getCost());
            }
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

    // ---- PlantPlacer
    // ---------------------------------------------------------------------
    // Same rules as NormalMode, restricted to the defense side of the red line.

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, PlantCard card) {
        if (card == null) {
            context.log("[Placement Failed] Selected card is null.");
            return false;
        }
        if (col < 0 || col >= redLineColumn) {
            context.log(
                    "[Placement Failed] Plants can only be placed left of the red line (col < " + redLineColumn + ").");
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

    /**
     * Guest-side per-tick update for sun producer zombies.
     * Called from GameController on the guest (zombie) side since the guest
     * never runs GameEngine.update() → updateMode().
     */
    public void updateSunProducers(GameContext context, float dt) {
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
    }
}
