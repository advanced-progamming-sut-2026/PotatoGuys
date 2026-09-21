package com.pvz.models.games.modes.variants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
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
    private final Random random = new Random();
    private GameContext cachedContext;
    private List<VaseBehavior> vases;
    private int cols;
    private int greenPots;
    private int gargantuarPots;
    private int xOffset;
    private MyPlant defaultPlant;
    private boolean lost;

    public VaseBreakerMode(Level level) {

        if (level instanceof VaseBreakerLevel vaseLevel) {
            this.plantPool = vaseLevel.getBasedPlants();
            this.zombiePool = vaseLevel.getBasedZombies();
            this.defaultPlant = vaseLevel.getDefaultPlant();
            this.cols = vaseLevel.getCols();

            this.greenPots = vaseLevel.getGreenPots();
            this.gargantuarPots = vaseLevel.getGargantuarPots();
            this.xOffset = vaseLevel.getXOffset();

            vases = new ArrayList<>();
        } else {
            this.plantPool = List.of();
            this.zombiePool = List.of();
        }
    }

    @Override
    public void initMode(GameContext ctx) {
        this.cachedContext = ctx;

        int gameMapLanes = ctx.getMap().getLanes();
        int gameMapCols = ctx.getMap().getColumns();

        // تعداد کل کوزه‌ها
        int totalVases = gameMapLanes * cols;

        // ۱. ساخت لیست اولیه از نوع NORMAL
        List<VaseType> vaseTypes = new ArrayList<>(Collections.nCopies(totalVases, VaseType.NORMAL));

        // ۲. آماده‌سازی اندیس‌ها برای انتخاب تصادفی
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < totalVases; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, random);

        // ۳. تخصیص کوزه‌های سبز (PLANT)
        int idx = 0;
        for (int i = 0; i < greenPots; i++) {
            vaseTypes.set(indices.get(idx++), VaseType.PLANT);
        }

        // ۴. تخصیص کوزه‌های غول (GARGANTUAR)
        for (int i = 0; i < gargantuarPots; i++) {
            vaseTypes.set(indices.get(idx++), VaseType.GARGANTUAR);
        }
        // بقیه اندیس‌ها همان NORMAL می‌مانند

        // ۵. ایجاد کوزه‌ها روی نقشه با نوع‌های تعیین‌شده
        int counter = 0;
        for (int i = 0; i < gameMapLanes; i++) {
            // حلقه از سمت راست به چپ، به اندازه cols ستون
            for (int j = gameMapCols - 1 - xOffset; j > gameMapCols - 1 - cols - xOffset; j--) {
                VaseType currentType = vaseTypes.get(counter++);
                Tile tile = ctx.getMap().getTileAt(j, i);
                VaseBehavior vaseBehavior = new VaseBehavior(currentType, tile);
                vases.add(vaseBehavior);
            }
        }
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(defaultPlant.getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, defaultPlant.getLevel());
        ctx.addCard(new PlantCard(defaultPlant, stats.getSunCost(), stats.getRechargeSeconds()));
        // ctx.addCard(null);
        ctx.log("gamemap lanes: " + gameMapLanes);
        ctx.log("gamemap cols: " + gameMapCols);
        ctx.log("vase cols: " + cols);
        ctx.log("xOffset: " + xOffset);
        ctx.log("green vases: " + greenPots);
        ctx.log("gargantuar vases: " + gargantuarPots);
        ctx.log("vases size: " + vases.size());
        ctx.log("vases type size: " + vaseTypes.size());
    }

    @Override
    public void updateMode(GameContext context, float dt) {
        if (lost) {
            context.setGameOver(true);
            return;
        }

        if (!anyVasesRemain() && context.getZombies().isEmpty()) {
            context.setGameOver(true);
            context.log("VICTORY! All vases cleared and all zombies defeated!");
            return;
        }

        for (int i = context.getZombies().size() - 1; i >= 0; i--) {
            Zombie z = context.getZombies().get(i);
            if (z.getX() <= GameController.colToWorldX(-1)) {
                lost = true;
                context.setGameOver(true);
                context.log("The zombie ate your brain; LOOSER!!!");
                context.removeZombie(z);
            }
        }
    }

    public boolean hasLost() {
        return lost;
    }

    @Override
    public void breakVase(GameContext context, int col, int lane) {
        Tile tile = context.getTileAt(col, lane);
        if (tile == null)
            return;

        VaseBehavior vase = findVaseBehavior(tile);
        if (vase == null || vase.isBroken()) {
            context.log("There is no intact vase at (" + col + ", " + lane + ").");
            return;
        }

        vase.breakVase();
        vases.remove(vase);
        context.log("Vase at (" + col + ", " + lane + ") shattered!");

        spawnRandomContent(context, vase.getVaseType(), col, lane);
    }

    private VaseBehavior findVaseBehavior(Tile tile) {
        for (var b : tile.getBehaviors()) {
            if (b instanceof VaseBehavior vb)
                return vb;
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
        Zombie zombie = new ZombieFactory().create(type.getAlias(), GameController.colToWorldX(col), lane, context, 0,
                1);
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

    private boolean anyVasesRemain() {
        if (cachedContext == null)
            return false;
        return !vases.isEmpty();
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
        if (card.getPlant() != defaultPlant) {
            context.removeCard(card);
        }
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
    public boolean supportsFallingSuns() {
        return false;
    }
}
