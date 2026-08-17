package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.pvz.controller.game.GameController;
import com.pvz.models.Constants;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.sun.SunType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.variants.IZombieLevel;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.ZombiePlacer;
import com.pvz.models.user.MyPlant;

public class IZombieMode implements GameMode, ZombiePlacer {
    private final List<MyPlant> basedPlants;
    private final List<ZombieType> basedZombies;
    private final boolean[] brainsEaten;
    private int redLineColumn = 6;

    private int ticksElapsed = 0;
    private boolean sunProducersSpawned = false;
    private final java.util.Map<Zombie, Float> brainEatTimers = new java.util.IdentityHashMap<>();
    private static final float BRAIN_EAT_DURATION = 3.0f;

    private static final String SUN_PRODUCER_ALIAS = "ZombieTutorialArmor2Default";
    private static final int SUN_PRODUCER_HP = 1290;
    private static final int SUN_AMOUNT = 25;
    private int sunProducerIntervalTicks = 300;
    private int sunProducerCountdown = 300;
    private final List<Zombie> sunProducers = new ArrayList<>();

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
        this.brainsEaten = new boolean[5];
    }

    public boolean[] getBrainsEaten() { return brainsEaten; }
    public int getRedLineColumn() { return redLineColumn; }
    public List<Zombie> getSunProducers() { return sunProducers; }

    @Override
    public boolean supportsFallingSuns() { return false; }

    @Override
    public void initMode(GameContext context) {
        Random random = new Random();
        int lanes = context.getMap().getLanes();
        int plantEndCol = redLineColumn - 1;

        if (basedPlants != null && !basedPlants.isEmpty()) {
            for (int lane = 0; lane < lanes; lane++) {
                boolean planted = false;
                for (int col = 1; col <= plantEndCol; col++) {
                    if (random.nextFloat() < 0.45f) {
                        MyPlant rp = basedPlants.get(random.nextInt(basedPlants.size()));
                        Plant plant = new PlantFactory().create(rp.getType(), col, lane, rp.getLevel(), rp.isBoosted(), context);
                        if (plant == null) continue;
                        context.spawnPlant(plant);
                        planted = true;
                    }
                }
            if (!planted) {
                int col = 1 + random.nextInt(Math.max(1, plantEndCol));
                MyPlant rp = basedPlants.get(random.nextInt(basedPlants.size()));
                Plant plant = new PlantFactory().create(rp.getType(), col, lane, rp.getLevel(), rp.isBoosted(), context);
                if (plant != null) {
                    context.spawnPlant(plant);
                }
            }
            }
        }

        setupZombieCards(context);
        context.log("I, Zombie ready! " + basedZombies.size() + " zombie types available.");
    }

    private void setupZombieCards(GameContext context) {
        if (basedZombies == null || basedZombies.isEmpty()) return;

        for (ZombieType zt : basedZombies) {
            if (zt == null) continue;
            int cost = ZOMBIE_SUN_COSTS.getOrDefault(zt, 50);
            float cooldown = Math.max(3f, Math.min(12f, 2f + cost / 50f));
            ZombieCard card = new ZombieCard(zt, cost, cooldown);
            card.setCooldownEnable(false);
            context.addCard(card);
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
                    context.spawnZombie(producer);
                    sunProducers.add(producer);
                } catch (Exception e) {
                    Gdx.app.error("IZombieMode", "Failed to spawn sun producer: " + e.getMessage());
                }
            }
        }

        List<Zombie> toRemove = new ArrayList<>();
        for (Zombie z : context.getZombies()) {
            if (z.isDead() || sunProducers.contains(z)) continue;
            float brainX = GameController.colToWorldX(0);
            if (z.getX() <= brainX + 10f) {
                int lane = GameController.worldYtoLane(z.getY());
                if (lane >= 0 && lane < brainsEaten.length && !brainsEaten[lane]) {
                    if (!brainEatTimers.containsKey(z)) {
                        brainEatTimers.put(z, BRAIN_EAT_DURATION);
                        z.setVelocity(0, 0);
                    }
                    float remaining = brainEatTimers.get(z) - dt;
                    brainEatTimers.put(z, remaining);
                    if (remaining <= 0) {
                        brainsEaten[lane] = true;
                        toRemove.add(z);
                    }
                }
            }
        }
        for (Zombie z : toRemove) {
            brainEatTimers.remove(z);
            context.removeZombie(z);
        }

        boolean allBrainsEaten = true;
        for (boolean eaten : brainsEaten) {
            if (!eaten) { allBrainsEaten = false; break; }
        }
        if (allBrainsEaten) {
            context.setGameOver(true);
            return;
        }

        sunProducerCountdown--;
        if (sunProducerCountdown <= 0) {
            spawnSunDrop(context);
            sunProducerCountdown = sunProducerIntervalTicks;
            if (sunProducerIntervalTicks > 120) {
                sunProducerIntervalTicks = Math.max(120, sunProducerIntervalTicks - 10);
            }
        }

        boolean hasLivingPlayerZombies = false;
        boolean hasLivingSunProducers = false;
        for (Zombie z : context.getZombies()) {
            if (z.isDead()) continue;
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
                context.setGameOver(true);
            }
        }
    }

    private void spawnSunDrop(GameContext context) {
        Random rng = new Random();
        int lane = rng.nextInt(context.getMap().getLanes());
        int col = 1 + rng.nextInt(Math.max(1, redLineColumn - 1));
        Sun sun = new Sun(SunType.NORMAL, col, lane, SUN_AMOUNT, true, context);
        context.spawnSun(sun);
    }

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        if (card == null || !(card instanceof ZombieCard zombieCard)) return false;
        if (col < redLineColumn || col >= context.getMap().getColumns()) return false;
        if (lane < 0 || lane >= context.getMap().getLanes()) return false;
        if (!zombieCard.canUse()) return false;
        if (context.getCurrentSun() < zombieCard.getCost()) return false;
        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (!(card instanceof ZombieCard zombieCard)) return;
        if (!context.spendSun(zombieCard.getCost())) {
            Gdx.app.log("IZombieMode", "Not enough sun for " + zombieCard.getZombieType());
            return;
        }
        try {
            float spawnX = GameController.colToWorldX(col);
            Zombie zombie = new ZombieFactory().create(zombieCard.getZombieType().getAlias(), spawnX, lane, context, 1, 1);
            context.spawnZombie(zombie);
            zombieCard.use();
        } catch (Exception e) {
            Gdx.app.error("IZombieMode", "Failed to place " + zombieCard.getZombieType() + ": " + e.getMessage());
            context.addSun(zombieCard.getCost());
        }
    }

    @Override
    public ZombieCard findCard(GameContext context, String zombieType) {
        for (Card card : context.getCards()) {
            if (card instanceof ZombieCard zc && zc.getZombieType().toString().equalsIgnoreCase(zombieType)) {
                return zc;
            }
        }
        return null;
    }

    @Override
    public String getCardsStatus(GameContext context) {
        StringBuilder sb = new StringBuilder();
        for (Card card : context.getCards()) {
            if (card instanceof ZombieCard zc) {
                sb.append(String.format("%s | %d sun%n", zc.getZombieType(), zc.getCost()));
            }
        }
        return sb.toString();
    }
}
