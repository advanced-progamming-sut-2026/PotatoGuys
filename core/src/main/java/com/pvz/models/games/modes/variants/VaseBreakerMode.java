package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.data.VaseDefinition;
import com.pvz.models.games.levels.data.VaseType;
import com.pvz.models.games.levels.variants.VaseBreakerLevel;
import com.pvz.models.games.map.behaviors.VaseBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;
import com.pvz.models.games.modes.capabilities.VaseBreaker;
import com.pvz.models.user.MyPlant;

public class VaseBreakerMode implements GameMode, VaseBreaker, PlantPlacer {

    private final List<MyPlant> plantPool;
    private final List<ZombieType> zombiePool;
    private final Boolean[] lawnMower;
    private final Random random = new Random();
    private GameContext cachedContext;

    public VaseBreakerMode(Level level) {
        int rows = level.getGameMapDefinition().rows;

        if (level instanceof VaseBreakerLevel vaseLevel) {
            this.plantPool = vaseLevel.getBasedPlants();
            this.zombiePool = vaseLevel.getBasedZombies();

            if (vaseLevel.getVases() != null) {
                for (VaseDefinition def : vaseLevel.getVases()) {
                    vasesToPlace.add(def);
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

    private final List<VaseDefinition> vasesToPlace = new ArrayList<>();

    @Override
    public void initMode(GameContext context) {
        this.cachedContext = context;
        for (VaseDefinition def : vasesToPlace) {
            Tile tile = context.getTileAt(def.getCol(), def.getLane());
            if (tile != null) {
                tile.addBehavior(new VaseBehavior(def.getVaseType()));
            }
        }
        context.log("Vasebreaker level started! Click vases to break them.");
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        if (!anyVasesRemain() && context.getZombies().isEmpty()) {
            context.setGameOver(true);
            context.log("VICTORY! All vases cleared and all zombies defeated!");
            return;
        }

        for (int i = context.getZombies().size() - 1; i >= 0; i--) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= GameController.colToWorldX(-1)) {
                int lane = GameController.worldYtoLane(z.getY());
                if (!lawnMower[lane]) {
                    runLawnMowers(context, lane);
                    continue;
                }
                context.setGameOver(true);
                context.log("The zombie ate your brain; LOOSER!!!");
                context.removeZombie(z);
            }
        }

        for (int i = context.getCards().size() - 1; i >= 0; i--) {
            Card card = context.getCards().get(i);
            if (card.getCooldown() <= 0.01f) {
                context.removeCard(card);
            }
        }
    }

    @Override
    public void breakVase(GameContext context, int col, int lane) {
        Tile tile = context.getTileAt(col, lane);
        if (tile == null) return;

        VaseBehavior vase = findVaseBehavior(tile);
        if (vase == null || vase.isBroken()) {
            context.log("There is no intact vase at (" + col + ", " + lane + ").");
            return;
        }

        vase.breakVase();
        context.log("Vase at (" + col + ", " + lane + ") shattered!");

        spawnRandomContent(context, vase.getVaseType(), col, lane);
    }

    private VaseBehavior findVaseBehavior(Tile tile) {
        for (var b : tile.getBehaviors()) {
            if (b instanceof VaseBehavior vb) return vb;
        }
        return null;
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

        double roll = random.nextDouble();
        if (roll < 0.45) {
            if (!zombiePool.isEmpty()) {
                ZombieType randomZombie = zombiePool.get(random.nextInt(zombiePool.size()));
                spawnZombie(context, randomZombie, col, lane);
            }
        } else if (roll < 0.85) {
            spawnRandomPlantCard(context);
        } else if (roll < 0.95) {
            context.addSun(50);
            context.log("Collected 50 Sun from the vase!");
        } else {
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
        for (int lane = 0; lane < context.getMap().getLanes(); lane++) {
            for (int col = 0; col < context.getMap().getColumns(); col++) {
                Tile tile = context.getTileAt(col, lane);
                VaseBehavior vase = findVaseBehavior(tile);
                if (vase != null && !vase.isBroken()) {
                    context.log(String.format("Vase at (%d, %d) -> Type: %s", col, lane, vase.getVaseType()));
                }
            }
        }
    }

    private boolean anyVasesRemain() {
        if (cachedContext == null) return false;
        for (int lane = 0; lane < cachedContext.getMap().getLanes(); lane++) {
            for (int col = 0; col < cachedContext.getMap().getColumns(); col++) {
                Tile tile = cachedContext.getTileAt(col, lane);
                VaseBehavior vase = findVaseBehavior(tile);
                if (vase != null && !vase.isBroken()) return true;
            }
        }
        return false;
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane]) return;
        context.getZombiesInLane(lane).forEach(z -> {
            z.takeDamage(Float.MAX_VALUE);
            context.removeZombie(z);
            context.log("Lawn mower in lane " + lane + " ran over a zombie!");
        });
        lawnMower[lane] = true;
    }

    // -- PlantPlacer capability --

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
        Tile tile = context.getTileAt(col, lane);
        VaseBehavior vase = findVaseBehavior(tile);
        if (vase != null && !vase.isBroken()) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") has an intact vase. Break it first!");
            return false;
        }
        if (!tile.isPlantable(card)) {
            context.log("[Placement Failed] Tile (" + col + ", " + lane + ") does not support planting "
                        + card.getPlant().getType());
            return false;
        }
        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, PlantCard card) {
        Plant plant = new PlantFactory().create(card.getPlant().getType(), col, lane,
                card.getPlant().getLevel(), card.getPlant().isBoosted(), context);
        context.spawnPlant(plant);
        context.getGameStats().onPlantPlaced(col, lane, card.getPlant().getType());
        context.removeCard(card);
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
                    result.append(String.format("\n- %s | Lvl:%d", ps.getPlant().getType(),
                            ps.getPlant().getLevel()));
                }
            }
        }
        return result.toString();
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
    }
}
