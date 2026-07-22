package pvz.controller.game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import pvz.models.AppContext;
import pvz.models.engine.TickAware;
import pvz.models.entities.plants.Plant;
import pvz.models.entities.projectile.Projectile;
import pvz.models.entities.sun.Sun;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;
import pvz.models.games.card.Card;
import pvz.models.games.card.PlantCard;
import pvz.models.games.card.ZombieCard;
import pvz.models.games.levels.variants.VaseBreakerLevel;
import pvz.models.games.map.behaviors.TileBehavior;
import pvz.models.games.map.tile.Tile;
import pvz.models.games.map.tile.TileTags;
import pvz.models.games.modes.GameMode;
import pvz.models.games.modes.capabilities.PlantPlacer;
import pvz.models.games.modes.capabilities.StartWaves;
import pvz.models.games.modes.capabilities.ZombiePlacer;
import pvz.models.games.modes.variants.VaseBreakerMode;
import pvz.models.quests.QuestEvaluator;
import pvz.models.user.User;
import pvz.view.MainMenu;
import pvz.view.Result;

public class GameController {
    
    private final GameContext context;

    public GameController(GameContext context) {
        this.context = context;
    }

    // =========================================================================
    // ─── CORE GAMEPLAY COMMANDS ──────────────────────────
    // =========================================================================

    public Result advanceTime(Matcher matcher) {
        int ticks = Integer.parseInt(matcher.group("ticks"));
        context.getEngine().advanceTime(ticks);
        context.addCurrentTick(ticks);

        String map = context.getMode().renderMap(context);
        if (context.isGameOver()) {
            User user = AppContext.getInstance().getCurrentUser();
            if (user != null) {
                boolean won = context.getZombies().isEmpty();
                QuestEvaluator.evaluateAll(
                    user.getQuestLog(),
                    context.getGameStats(),
                    won,
                    context.getCurrentSun(),
                    user.getSetting().getDifficulty(),
                    user,
                    context
                );
                if (won) {
                    user.getScore().setLastLevel(context.getLevelNumber());
                    user.getScore().setLastSeason(0);
                    
                    // Unlock next level
                    pvz.models.games.seasons.SeasonManager manager = new pvz.models.games.seasons.SeasonManager();
                    manager.unlockNextLevel(user, context.getSeasonName(), context.getLevelNumber());
                }
                user.saveUser();
            }
            return new Result("Game Over", new MainMenu());
        }
        return new Result("Time advanced by " + ticks + " ticks.\n" + map);
    }

    public Result plantPlant(Matcher matcher) {
        String plantType = matcher.group("plantType");
        int col = Integer.parseInt(matcher.group("plantX"));
        int lane = Integer.parseInt(matcher.group("plantY"));

        GameMode mode = context.getMode();
        if (mode instanceof PlantPlacer placer) {
            PlantCard card = placer.findCard(context, plantType);
            if (card == null) {
                return new Result("No such plant card '" + plantType + "'.");
            }
            if (!placer.isValidPlacement(context, col, lane, card)) {
                return new Result("Cannot place " + plantType + " at (" + col + ", " + lane + ").");
            }
            placer.handlePlacement(context, col, lane, card);
        } else {
            return new Result("You cannot plant in this game mode!");
        }
        return new Result(plantType + " placed at (" + col + ", " + lane + ").");
    }

    public Result plantZombie(Matcher matcher) {
        String zombieType = matcher.group("zombieType");
        int col = Integer.parseInt(matcher.group("zombieX"));
        int lane = Integer.parseInt(matcher.group("zombieY"));

        GameMode mode = context.getMode();
        if (mode instanceof ZombiePlacer placer) {
            ZombieCard card = placer.findCard(context, zombieType);
            if (card == null) {
                return new Result("No such zombie card '" + zombieType + "'.");
            }
            if (!placer.isValidPlacement(context, col, lane, card)) {
                return new Result("Cannot place " + zombieType + " at (" + col + ", " + lane + ").");
            }
            placer.handlePlacement(context, col, lane, card);
        } else {
            return new Result("You cannot place zombies in this game mode!");
        }
        return new Result(zombieType + " placed at (" + col + ", " + lane + ").");
    }

    public Result breakVase(Matcher matcher){
        GameMode mode = context.getMode();
        int col = Integer.parseInt(matcher.group("vaseX"));
        int lane = Integer.parseInt(matcher.group("vaseY"));

        if(mode instanceof VaseBreakerMode vasemode){
            vasemode.breakVase(context, col, lane);
            return new Result("");
        }
        
        return new Result("You cannot break vase in this game mode!");
    }

    public Result pluckPlant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("pluckX"));
        int y = Integer.parseInt(matcher.group("pluckY"));
        
        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
             return new Result("Invalid coordinates.");
        }
        
        Tile tile = context.getMap().getTile(x, y);
        if (tile.getPlants().isEmpty()) {
            return new Result("No plant to pluck at (" + x + ", " + y + ").");
        }

        for (int i = 0; i < tile.getPlants().size(); i++) {
            context.removePlant(tile.getPlants().get(i));
            i--;
        }
        tile.getPlants().clear();
        
        return new Result("Plant plucked from (" + x + ", " + y + ").");
    }

    public Result collectSun(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("sunX"));
        int y = Integer.parseInt(matcher.group("sunY"));
        
        for (Sun sun : new ArrayList<>(context.getSuns())) {
            if (sun.getCol() == x && sun.getLane() == y && !sun.isDone()) {
                sun.collect(context);
                return new Result("Sun collected!");
            }
        }
        return new Result("No sun found at these coordinates.");
    }

    public Result feedPlant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("feedX"));
        int y = Integer.parseInt(matcher.group("feedY"));

        if (context.getPlantFoodCount() <= 0) {
            return new Result("No plant food available.");
        }
        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
            return new Result("Invalid coordinates.");
        }

        List<Plant> plantsAt = context.getPlantsAt(x, y);
        if (plantsAt.isEmpty()) {
            return new Result("No plant at (" + x + ", " + y + ").");
        }

        Plant target = plantsAt.getLast();
        if (!context.spendPlantFood()) {
            return new Result("Failed to consume plant food.");
        }

        target.triggerPlantFood(context);
        return new Result("Plant food used on " + target.getSheet().getName() + " at (" + x + ", " + y + ")!");
    }

    public Result startZombieWavesCommand(Matcher matcher) {
        GameContext context = AppContext.getInstance().getGameContext();
        
        if (context.getMode() instanceof StartWaves mode) {
            if(!mode.isPreparationPhase()) {
                return new Result("Zombie waves have already started!");
            }
            mode.startZombieWaves(context);
            return new Result("Zombie waves started successfully!");
        } else {
            return new Result("This command is only available in modes that support starting zombie waves.");
        }
    }


    // =========================================================================
    // ─── CHEAT & DEBUG COMMANDS ─────────────────
    // =========================================================================

    public Result releaseNuke(Matcher matcher) {
        int killed = 0;
        List<TickAware> entities = context.getEngine().getEntities();
        for (TickAware entity : entities) {
            if (entity instanceof Zombie) {
                ((Zombie) entity).takeDamage(999999f, true);
                killed++;
            }
        }
        return new Result("Nuke released! Killed " + killed + " zombies.");
    }

    public Result cheatCooldown(Matcher matcher) {
        List<Card> cards = context.getCards();
        int reset = 0;
        for (Card card : cards) {
            if (card.getCooldown() > 0) {
                card.setCooldown(0);
                reset++;
            }
        }
        return new Result("Reset cooldown for " + reset + " card(s).");
    }

    public Result cheatPlantFood(Matcher matcher) {
        if (context.getPlantFoodCount() > 3) {
            return new Result("Plant food slots are full.");
        }
        context.addPlantFood(1);
        return new Result("Added 1 plant food.");
    }

    public Result cheatSun(Matcher matcher) {
        int amount = Integer.parseInt(matcher.group("sunCount"));
        context.addSun(amount);
        return new Result("Added " + amount + " sun.");
    }

    public Result cheatSpawnZombie(Matcher matcher) {
        // متد هنوز پیاده‌سازی نشده است
        return new Result("");
    }


    // =========================================================================
    // ─── ۳. SHOW & QUERY COMMANDS (دستورات نمایشی و گزارش‌گیری) ──────────────────
    // =========================================================================

    public Result showMap(Matcher matcher) {
        return new Result(context.getMode().renderMap(context));
    }

    public Result showCards(Matcher matcher) {
        return new Result(context.getMode().getCardsStatus(context));
    }

    public Result showSun(Matcher matcher) {
        return new Result("Current Sun: " + context.getCurrentSun());
    }

    public Result showTileStatus(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("tileX"));
        int y = Integer.parseInt(matcher.group("tileY"));

        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
             return new Result("Invalid coordinates.");
        }

        Tile tile = context.getMap().getMap()[y][x];
        StringBuilder status = new StringBuilder("Tile Status at (").append(x).append(",").append(y).append("):");

        //--- Tile:
        if (tile.getTags().isEmpty()) status.append("\n  Tags: Normal");
        else {
            status.append("\n  Tags:");
            for (TileTags tag : tile.getTags()) {
                status.append("\n    ").append(tag.toString());
            }
        }

        if (!tile.getBehaviors().isEmpty()) {
            status.append("\n  Status:");
            for (TileBehavior tb : tile.getBehaviors()) {
                status.append(tb.getStatus());
            }
        }

        //--- Plant:
        if (tile.getPlants() != null && !tile.getPlants().isEmpty()) {
            status.append("\n").append("  Plant: ").append(tile.getPlants().getLast().getType());
        } else {
            status.append("\n").append("  Plant: None");
        }

        //-- Zombies:
        if (!context.getZombiesAt(x,y).isEmpty()) {
            status.append("\n").append("  Zombies: ");
            for (Zombie z : context.getZombiesAt(x, y)) {
                status.append("\n  ").append(z.getSheet().getAlias());
            }
        } else {
            status.append("\n").append("  Zombies: None");
        }

        return new Result(status.toString());
    }

    public Result showPlants(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Plants List:\n");
        int count = 0;
        int rows = context.getMap().getRows();
        int cols = context.getMap().getColumns();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile tile = context.getMap().getMap()[i][j];
                if (tile.getPlants() != null && !tile.getPlants().isEmpty()) {
                    Plant plant = tile.getPlants().get(tile.getPlants().size() - 1);
                    if (plant != null && !plant.isDead()) {
                        String stateLabel = plant.getCurrentState() != null ? plant.getCurrentState().getLabel() : "Unknown";
                        
                        sb.append(String.format("- %s | Position: (%d, %d) | HP: %.0f/%.0f | State: %s\n",
                                plant.getSheet().getName(),
                                plant.getCol(),
                                plant.getLane(),
                                plant.getHp(),
                                plant.getMaxHp(),
                                stateLabel
                        ));
                        count++;
                    }
                }
            }
        }
        if (count == 0) {
            return new Result("No plants on the map.");
        }
        return new Result(sb.toString().trim());
    }

    public Result showZombies(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Zombies List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Zombie zombie) {
                String stateLabel = zombie.getCurrentState() != null ? zombie.getCurrentState().getLabel() : "Unknown";
                
                sb.append(String.format("- %s | Position: (%.1f, %d) | HP: %.0f/%.0f | State: %s%s\n",
                        zombie.getSheet().getAlias(),
                        zombie.getX(),
                        zombie.getLane(),
                        zombie.getHp(),
                        zombie.getMaxHp(),
                        stateLabel,
                        zombie.isGlowing() ? " [GLOWING]" : ""
                ));
                count++;
            }
        }
        if (count == 0) {
            return new Result("No zombies on the map.");
        }
        return new Result(sb.toString().trim());
    }

    public Result showProjectiles(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Projectiles List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Projectile projectile) {
                if (!projectile.isSpent()) {
                    sb.append(String.format("- %s | Position: (%.1f, %d) | Damage: %.1f | State: Active\n",
                            projectile.getType(),
                            projectile.getCol(),
                            projectile.getLane(),
                            projectile.getDamage()
                    ));
                    count++;
                }
            }
        }
        if (count == 0) {
            return new Result("No projectiles on the map.");
        }
        return new Result(sb.toString().trim());
    }

    public Result showSuns(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Dropped Suns List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Sun sun) {
                if (!sun.isDone()) {
                    sb.append(String.format("- %s | Position: (%d, %d) | Sun Amount: %d | Expiry: %.1fs\n",
                            sun.getType(),
                            sun.getCol(),
                            sun.getLane(),
                            sun.getAmount(),
                            sun.getSecondsRemaining()
                    ));
                    count++;
                }
            }
        }
        if (count == 0) {
            return new Result("No suns on the map.");
        }
        return new Result(sb.toString().trim());
    }
}