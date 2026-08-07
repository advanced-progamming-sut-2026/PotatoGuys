package com.pvz.models.games.modes.variants;

import java.util.List;

import com.pvz.enums.AnsiColors;
import com.pvz.models.Constants;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.effects.ChapterEffect;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.levels.variants.NormalLevel;
import com.pvz.models.games.map.behaviors.DestructibleBehavior;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;
import com.pvz.models.games.modes.GameMode;
import com.pvz.models.games.modes.capabilities.PlantPlacer;

/**
 * Standard game mode implementation.
 * Manages waves and standard win/loss conditions.
 */
public class NormalMode implements GameMode, PlantPlacer {
    private Wave currentWave;
    private List<Wave> waves;
    private Boolean[] lawnMower;

    public NormalMode(Level level) {
        if (level instanceof NormalLevel normalLevel) {
            waves = normalLevel.getWaves();
        }
        currentWave = waves.getFirst();
        setupLawnMowers();
    }

    @Override
    public void initMode(GameContext context) {
        if (currentWave != null) {
            currentWave.startWave(context);
            for (ChapterEffect effect : context.getActiveEffects()) {
                effect.onWaveStart(currentWave, context);
            }
        }
    }

    @Override
    public void updateMode(GameContext context) {

        if (currentWave.isDone() && context.getZombies().isEmpty()) {
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
                //context.log("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
            return;
        }

        if (!currentWave.isDone()) {
            currentWave.updateWave(context);
        }

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
                    context.log("The zombie ate your brain; LOOSER!!!");
                    context.removeZombie(z);
                }
            }
        }
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
        if (card == null) {
            context.log("[Placement Failed] Selected card is null.");
            return false;
        }

        if (col < 0 || col >= context.getColumns() || lane < 0 || lane >= context.getLanes()) {
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

        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
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
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        ResolvedStats stats = PlantStatResolver.resolve(sheet, card.getPlant().getLevel());
        if (!context.spendSun(stats.getSunCost())) {
            context.log("Not enough sun.");
            return;
        }
        Plant plant = new PlantFactory().create(plantCard.getPlant().getType(), col, lane,
                plantCard.getPlant().getLevel(), plantCard.getPlant().isBoosted(), context);
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
                        (float) ps.getCooldown() / (float) Constants.TICK_PER_SECOND,
                        ps.getPlant().isBoosted() ? " | ⚡B" : "");

                result.append("\n");
                result.append(String.format("%-" + cardWidth + "s", cardInfo));
            }
        }
        return result.toString();
    }

    private void setupLawnMowers() {
        int lanes = 5;
        lawnMower = new Boolean[lanes];
        for (int i = 0; i < lanes; i++) {
            lawnMower[i] = false;
        }
    }

    private void runLawnMowers(GameContext context, int lane) {
        if (lawnMower[lane])
            return;

        context.getZombiesInLane(lane).forEach(zombie -> {
            zombie.takeDamage(Float.MAX_VALUE);
            context.removeZombie(zombie);
            context.log("Lawn mower in lane " + lane + " ran over a zombie!");
        });
        lawnMower[lane] = true;
    }

    private static final String CELL_EMPTY = "    ";
    private static final String MOWER_OK = "[M]";
    private static final String MOWER_USED = "[!]";

    @Override
    public String renderMap(GameContext context) {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb, context);
        appendColumnHeaders(sb, context);
        appendDivider(sb, context);
        for (int lane = 0; lane < context.getLanes(); lane++) {
            appendLaneRow(sb, context, lane);
            appendDivider(sb, context);
        }
        return sb.toString();
    }

    // ── Private rendering helpers ─────────────────────────────────────────────

    private void appendHeader(StringBuilder sb, GameContext context) {
        sb.append("\n=== Tick: ").append(context.getCurrentTick())
                .append(" | Sun: ").append(context.getCurrentSun())
                .append(" | Zombies: ").append(context.getZombies().size())
                .append(" | Plants: ").append(context.getPlants().size())
                .append(" | Projectiles: ").append(context.getProjectiles().size())
                .append(" | Suns: ").append(context.getSuns().size())
                .append(" | Plant foods: ").append(context.getPlantFoodCount())
                .append(" ===\n");
    }

    private void appendColumnHeaders(StringBuilder sb, GameContext context) {
        sb.append("\n     ");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append(String.format(" C%-2d ", c));
        }
        sb.append("\n");
    }

    private void appendDivider(StringBuilder sb, GameContext context) {
        sb.append("    +");
        for (int c = 0; c < context.getColumns(); c++) {
            sb.append("----+");
        }
        sb.append("\n");
    }

    private void appendLaneRow(StringBuilder sb, GameContext context, int lane) {
        sb.append(lawnMower[lane] ? MOWER_USED : MOWER_OK).append(" |");
        for (int col = 0; col < context.getColumns(); col++) {
            sb.append(getCellContent(col, context, lane)).append('|');
        }
        sb.append("  Lane ").append(lane).append("\n");
    }

    private String getCellContent(int col, GameContext context, int lane) {
        List<Plant> plantsAtCell = context.getPlantsAt(col, lane);
        List<Zombie> zombiesAtCell = context.getZombiesAt(col, lane);
        Tile tile = context.getTileAt(col, lane);

        if (tile != null) {
            String specialContent = getSpecialTileContent(tile, !plantsAtCell.isEmpty(), zombiesAtCell);
            if (specialContent != null) {
                return specialContent;
            }
        }

        return getStandardCellContent(tile, plantsAtCell, zombiesAtCell, context, col, lane);
    }

    private String getSpecialTileContent(Tile tile, boolean hasPlant, List<Zombie> zombiesAtCell) {
        boolean hasZombie = !zombiesAtCell.isEmpty();

        if (tile.getTags().contains(TileTags.GRAVE)) {
            String graveDisplay = " G  ";
            for (TileBehavior b : tile.getBehaviors()) {
                if (b instanceof DestructibleBehavior db) {
                    if (db.getReward() == DestructibleBehavior.GraveReward.SUN_50) {
                        graveDisplay = " G$ ";
                    } else if (db.getReward() == DestructibleBehavior.GraveReward.PLANT_FOOD) {
                        graveDisplay = " G! ";
                    }
                }
            }
            if (hasZombie) {
                return String.format(AnsiColors.BRIGHT_BLACK + "G" + AnsiColors.RESET + "/Z%-1d", zombiesAtCell.size());
            }
            return AnsiColors.BRIGHT_BLACK + graveDisplay + AnsiColors.RESET;
        } else if (tile.getTags().contains(TileTags.SLIP_UP)) {
            if (hasZombie) {
                return String.format(AnsiColors.BLUE + "↑" + AnsiColors.RESET + "/Z%-1d", zombiesAtCell.size());
            }
            return AnsiColors.BLUE + " S↑ " + AnsiColors.RESET;
        } else if (tile.getTags().contains(TileTags.SLIP_DOWN)) {
            if (hasZombie) {
                return String.format(AnsiColors.BLUE + "↓" + AnsiColors.RESET + "/Z%-1d", zombiesAtCell.size());
            }
            return AnsiColors.BLUE + " S↓ " + AnsiColors.RESET;
        } else if (tile.getTags().contains(TileTags.ICE_BLOCK)) {
            if (hasZombie) {
                return String.format(AnsiColors.BLUE + "I" + AnsiColors.RESET + "/Z%-1d", zombiesAtCell.size());
            } else if (hasPlant) {
                return AnsiColors.BLUE + "I/P " + AnsiColors.RESET;
            }
            return AnsiColors.BLUE + " I  " + AnsiColors.RESET;
        }
        return null;
    }

    private String getStandardCellContent(Tile tile, List<Plant> plantsAtCell, List<Zombie> zombiesAtCell, GameContext ctx, int col, int lane) {
        boolean hasPlant = !plantsAtCell.isEmpty();
        boolean hasZombie = !zombiesAtCell.isEmpty();
        List<Projectile> projectiles=ctx.getProjectiles()
                .stream()
                .filter(p->((p.getX()<col+1 && p.getX()>=col) && (p.getY()<lane+1 && p.getY()>=lane)))
                .toList();
        boolean hasProjectile=!projectiles.isEmpty();

        StringBuilder output = new StringBuilder();
        if (tile != null && tile.getTags().contains(TileTags.WATER)) {
            output.append(AnsiColors.BLUE_BG);
        }

        if (hasPlant && hasZombie) {
            output.append(String.format(AnsiColors.GREEN + "P" + AnsiColors.RESET + "/Z%-1d", plantsAtCell.size(),
                    zombiesAtCell.size()));
        } else if(hasZombie && hasProjectile){
            output.append(String.format("●/Z%-1d",zombiesAtCell.size()));
        } else if (hasPlant && hasProjectile){
            output.append(String.format(AnsiColors.GREEN + "P" + AnsiColors.RESET+"/●%-1d",projectiles.size()));
        } else if (hasProjectile){
            output.append(String.format(" ●%-1d ",projectiles.size()));
        } else if (hasPlant) {
            output.append(String.format(AnsiColors.GREEN + " P  " + AnsiColors.RESET, plantsAtCell.size()));
        } else if (hasZombie) {
            output.append(String.format(" Z%-2d", zombiesAtCell.size()));
        } else {
            output.append(CELL_EMPTY);
        }

        if (tile != null && tile.getTags().contains(TileTags.WATER)) {
            output.append(AnsiColors.RESET);
        }

        if (!output.isEmpty()) {
            return output.toString();
        }

        return CELL_EMPTY;
    }
}
