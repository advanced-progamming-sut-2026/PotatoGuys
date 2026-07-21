package pvz.Models.Games.Modes.variants;

import java.util.List;
import java.util.Random;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieFactory;
import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.variants.IZombieLevel;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.Modes.Capabilities.ZombiePlacer;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.ZombieCard;
import pvz.Models.User.MyPlant;

public class IZombieMode implements GameMode, ZombiePlacer {
    private final List<MyPlant> basedPlants;
    private final List<ZombieType> basedZombies;
    private final boolean[] brainsEaten;
    private int redLineColumn = 4;
    
    private int ticksElapsed = 0;
    private int sunZombieInterval = 150;

    public IZombieMode(Level level) {
        if (level instanceof IZombieLevel iZombieLevel) {
            this.basedPlants = iZombieLevel.getBasedPlants();
            this.basedZombies = iZombieLevel.getBasedZombies();
            this.redLineColumn = iZombieLevel.getRedLineColumn();
        } else {
            throw new IllegalArgumentException("Level must be an instance of IZombieLevel.");
        }

        this.brainsEaten = new boolean[5];
        for (int i = 0; i < 5; i++) {
            brainsEaten[i] = false;
        }
    }

    @Override
    public boolean supportsFallingSuns() {
        return false;
    }

    @Override
    public void initMode(GameContext context) {
        context.log("\n=========================================================");
        context.log("  I, ZOMBIE MODE ACTIVATED!");
        context.log("• Deploy zombies to eat all 5 brains!");
        context.log(        "• Placement: You can only place zombies to the right of Column " + (redLineColumn - 1));
        context.log("=========================================================\n");

        Random random = new Random();

        if (basedPlants != null && !basedPlants.isEmpty()) {
            for (int col = 0; col < redLineColumn; col++) {
                for (int lane = 0; lane < context.getLanes(); lane++) {
                    MyPlant randomPlant = basedPlants.get(random.nextInt(basedPlants.size()));

                    Plant plant = new PlantFactory().create(
                            randomPlant.getType(),
                            col,
                            lane,
                            randomPlant.getLevel(),
                            randomPlant.isBoosted(),
                            context
                    );
                    context.spawnPlant(plant);
                }
            }
            context.log("Plants have been randomly spawned on the board!");
        }

        setupRandomZombieCards(context, random);
    }


    private void setupRandomZombieCards(GameContext context, Random random) {
        if (basedZombies == null || basedZombies.isEmpty()) {
            context.log("⚠️ No base zombies defined for this level!");
            return;
        }

        int targetCards = Math.min(7, basedZombies.size());
        int addedCards = 0;
        int maxAttempts = 100; 

        while (addedCards < targetCards && maxAttempts > 0) {
            maxAttempts--;

            ZombieType randomZombieType = basedZombies.get(random.nextInt(basedZombies.size()));

            if (randomZombieType == null || randomZombieType.getAlias() == null) {
                continue;
            }

            if (findCard(context, randomZombieType.toString()) != null) {
                continue;
            }

            ZombieCard zombieCard = new ZombieCard(randomZombieType, 50, 5.0f);
            context.addCard(zombieCard);
            addedCards++;
        }

        context.log("🧟 " + addedCards + " Random Zombie cards selected for this match!");
    }

    @Override
    public void updateMode(GameContext context) {
        ticksElapsed++;

        if (ticksElapsed % 200 == 0 && sunZombieInterval > 40) {
            sunZombieInterval -= 10; 
        }

        // if (ticksElapsed % sunZombieInterval == 0) {
        //     long aliveSunZombies = context.getZombies().stream()
        //             .filter(z -> !z.isDead() && z.getType().toString().equalsIgnoreCase("SUN_ZOMBIE"))
        //             .count();
        //     if (aliveSunZombies > 0) {
        //         int sunProduced = (int) (aliveSunZombies * 25);
        //         context.addSun(sunProduced);
        //         context.log("☀️ Sun Zombies generated " + sunProduced + " Sun! Total Sun: " + context.getCurrentSun());
        //     }
        // }

        // ─── ۲. بررسی رسیدن زامبی‌ها به انتهای لاین (خوردن مغز) ─────────────────
        for (Zombie z : context.getZombies()) {
            if (!z.isDead() && z.getX() <= 0f) {
                int lane = z.getLane();
                if (!brainsEaten[lane]) {
                    brainsEaten[lane] = true;
                    context.log("🧠 BRAIN EATEN in Lane " + lane + "! Yummy!");
                    context.removeZombie(z);
                }
            }
        }

        // ─── ۳. بررسی شرط برد (خورده شدن تمامی ۵ مغز) ─────────────────────────
        boolean allBrainsEaten = true;
        for (boolean eaten : brainsEaten) {
            if (!eaten) {
                allBrainsEaten = false;
                break;
            }
        }

        if (allBrainsEaten) {
            context.setGameOver(true);
            context.log("🎉 VICTORY! You ate all the brains! Humanz are defeated! 🎉");
            return;
        }

        // ─── ۴. بررسی شرط باخت (عدم توانایی خرید زامبی + نبود زامبی در زمین) ───
        if (context.getZombies().isEmpty()) {
            int minZombieCost = getCheapestZombieCost(context);
            if (context.getCurrentSun() < minZombieCost) {
                context.setGameOver(true);
                context.log("❌ GAME OVER! Out of Sun and no zombies left on the field! ❌");
            }
        }
    }

    // ─── پیاده‌سازی ZombiePlacer ──────────────────────────────────────────────

    @Override
    public boolean isValidPlacement(GameContext context, int col, int lane, Card card) {
        if (col < redLineColumn || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
            context.log("Must place zombies to the right of Column " + (redLineColumn - 1) + "!");
            return false;
        }

        if (!(card instanceof ZombieCard zombieCard)) {
            return false;
        }

        if (!zombieCard.canUse()) {
            context.log("Zombie card is on cooldown!");
            return false;
        }

        if (context.getCurrentSun() < zombieCard.getCost()) {
            context.log("Not enough sun! Requires " + zombieCard.getCost() + " sun.");
            return false;
        }

        return true;
    }

    @Override
    public void handlePlacement(GameContext context, int col, int lane, Card card) {
        if (!(card instanceof ZombieCard zombieCard)) return;

        if (!context.spendSun(zombieCard.getCost())) {
            context.log("Error: Not enough sun.");
            return;
        }

        Zombie zombie = new ZombieFactory().create(zombieCard.getZombieType().getAlias(), col, lane,context , 1, 1);
        context.spawnZombie(zombie);

        zombieCard.use();
        context.log(zombieCard.getZombieType() + " deployed at (" + col + ", " + lane + ").");
    }

    @Override
    public ZombieCard findCard(GameContext context, String zombieType) {
        for (Card card : context.getCards()) {
            if (card instanceof ZombieCard zombieCard) {
                if (zombieCard.getZombieType().toString().equalsIgnoreCase(zombieType)) {
                    return zombieCard;
                }
            }
        }
        return null;
    }

    @Override
    public String getCardsStatus(GameContext context) {
        StringBuilder sb = new StringBuilder();
        List<Card> cards = context.getCards();

        if (cards == null || cards.isEmpty()) {
            sb.append("No zombie cards available.");
        } else {
            sb.append("=== ZOMBIE CARDS ===");
            int cardWidth = 40;

            for (Card card : cards) {
                if (card instanceof ZombieCard zc) {
                    String cardInfo = String.format("- %s | Cost:%d | Cooldown:%.1f",
                            zc.getZombieType(),
                            zc.getCost(),
                            zc.getCooldown()
                    );
                    sb.append("\n").append(String.format("%-" + cardWidth + "s", cardInfo));
                }
            }
        }
        return sb.toString();
    }

    private int getCheapestZombieCost(GameContext context) {
        int minCost = Integer.MAX_VALUE;
        for (Card card : context.getCards()) {
            if (card instanceof ZombieCard zc) {
                minCost = Math.min(minCost, zc.getCost());
            }
        }
        return minCost == Integer.MAX_VALUE ? 50 : minCost;
    }

    // =========================================================================
    // ─── سیستم رندر اختصاصی نقشه همراه با مغزها و خط قرمز ──────────────────────
    // =========================================================================

    private static final String CELL_EMPTY = "    ";
    private static final String BRAIN_OK = "[B]";
    private static final String BRAIN_EATEN = "[!]";

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n=== TICK: ").append(context.getCurrentTick())
          .append(" | SUN: ").append(context.getCurrentSun())
          .append(" | ACTIVE ZOMBIES: ").append(context.getZombies().size())
          .append(" | PLANTS LEFT: ").append(context.getPlants().size())
          .append(" ===\n");

        sb.append("\n     ");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append(String.format(" C%-2d ", c));
        }
        sb.append("\n");

        appendDivider(sb, context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            sb.append(brainsEaten[lane] ? BRAIN_EATEN : BRAIN_OK).append(" |");

            for (int col = 0; col < context.getColumns(); col++) {
                sb.append(getCellContent(col, context, lane));

                if (col == redLineColumn - 1) {
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
        List<Plant> plants = context.getPlantsAt(col, lane);
        List<Zombie> zombies = context.getZombiesAt(col, lane);

        boolean hasPlant = !plants.isEmpty();
        boolean hasZombie = !zombies.isEmpty();

        if (hasPlant && hasZombie) return "P/Z ";
        if (hasPlant) return " P  ";
        if (hasZombie) return String.format(" Z%-2d", zombies.size());
        return CELL_EMPTY;
    }
}