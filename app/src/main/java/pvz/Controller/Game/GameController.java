package pvz.Controller.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.Capabilities.ZombiePlacer;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.Games.card.ZombieCard;
import pvz.Models.Games.map.Tile;
import pvz.View.MainMenu;
import pvz.View.Result;

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

        Tile tile = context.getMap().getMap()[x][y];
        StringBuilder status = new StringBuilder("Tile Status at (").append(x).append(",").append(y).append("):");

        //--- Tile type:
        status.append("\n").append("  Type: ").append(tile.getType().typeName);

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