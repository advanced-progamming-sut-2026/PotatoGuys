package pvz.Controller.Game;

import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.map.Tile;
import pvz.View.MainMenu;
import pvz.View.Result;

public abstract class GameController {
    GameContext context;
    public GameController(GameContext context){
        this.context = context;
    }
    public Result advanceTime(Matcher matcher) {
        int ticks = Integer.parseInt(matcher.group("ticks"));
        context.getEngine().advanceTime(ticks);
        context.addCurrentTick(ticks);
        String map = context.getMode().renderMap(context);
        if(context.isGameOver()){
            return new Result("Game Over" , new MainMenu());
        }
        return new Result("Time advanced by " + ticks + " ticks.\n" + map);
    }
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

    public Result cheatCooldown(Matcher matcher) { return null; }

    public Result feedPlant(Matcher matcher) { return null; }

    public Result cheatPlantFood(Matcher matcher) {
        if (context.getPlantFoodCount()>3) return new Result("Plant food slots are full.");
        context.addPlantFood(1);
        return new Result("Added 1 plant food.");
    }

    public Result showMap(Matcher matcher) {
        return new Result(context.getMode().renderMap(context));
    }

    public Result showTileStatus(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("tileX"));
        int y = Integer.parseInt(matcher.group("tileY"));

        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
             return new Result("Invalid coordinates.");
        }

        Tile tile = context.getMap().getMap()[y][x];
        StringBuilder status = new StringBuilder("Tile Status at (").append(x).append(",").append(y).append("):\n");
        if (tile.getPlants() != null && tile.getPlants().getLast()!=null) {
            status.append("Plant: ").append(tile.getPlants().getLast().getType()).append("\n");
        } else {
            status.append("Plant: None\n");
        }
        //status.append("Zombies count: ").append(tile.getZombies().size()).append("\n");
        return new Result(status.toString());
    }
    public Result zombiesInfo(Matcher matcher) {
        StringBuilder info = new StringBuilder("Zombies Info:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Zombie) {
                Zombie zombie = (Zombie) entity;
                info.append(zombie.toInfoString()).append("\n");
                count++;
            }
        }
        if (count == 0) {
            return new Result("No zombies on the map.");
        }
        return new Result(info.toString());
    }
    public Result cheatSpawnZombie(Matcher matcher) {
        return new Result("");
    }

    // ─── SHOW ZOMBIES ──────────────────────────────────────────────────────────
    public Result showZombies(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Zombies List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Zombie) {
                Zombie zombie = (Zombie) entity;
                String stateLabel = zombie.getCurrentState() != null ? zombie.getCurrentState().getLabel() : "Unknown"; //[cite: 4]
                
                // فرمت: Name | Position: (X, Y) | HP: Current/Max | State: Status
                sb.append(String.format("- %s | Position: (%.1f, %d) | HP: %.0f/%.0f | State: %s%s\n",
                        zombie.getSheet().getAlias(), //[cite: 4]
                        zombie.getX(), //[cite: 4]
                        zombie.getLane(), //[cite: 4]
                        zombie.getHp(), //[cite: 4]
                        zombie.getMaxHp(), //[cite: 4]
                        stateLabel,
                        zombie.isGlowing() ? " [GLOWING]" : "" //[cite: 4]
                ));
                count++;
            }
        }
        if (count == 0) {
            return new Result("No zombies on the map.");
        }
        return new Result(sb.toString().trim());
    }

    // ─── SHOW PLANTS ───────────────────────────────────────────────────────────
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
                    if (plant != null && !plant.isDead()) { //[cite: 3]
                        String stateLabel = plant.getCurrentState() != null ? plant.getCurrentState().getLabel() : "Unknown"; //[cite: 3]
                        
                        // فرمت: Name | Position: (X, Y) | HP: Current/Max | State: Status
                        sb.append(String.format("- %s | Position: (%d, %d) | HP: %.0f/%.0f | State: %s\n",
                                plant.getSheet().getName(), //[cite: 3]
                                plant.getCol(), //[cite: 3]
                                plant.getLane(), //[cite: 3]
                                plant.getHp(), //[cite: 3]
                                plant.getMaxHp(), //[cite: 3]
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

    // ─── SHOW PROJECTILES ──────────────────────────────────────────────────────
    public Result showProjectiles(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Projectiles List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Projectile) {
                Projectile projectile = (Projectile) entity;
                if (!projectile.isSpent()) { //[cite: 1]
                    
                    // فرمت: Type | Position: (X, Y) | Damage: Value | State: Active
                    sb.append(String.format("- %s | Position: (%.1f, %d) | Damage: %.1f | State: Active\n",
                            projectile.getType(), //[cite: 1]
                            projectile.getCol(), //[cite: 1]
                            projectile.getLane(), //[cite: 1]
                            projectile.getDamage() //[cite: 1]
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

    // ─── SHOW SUNS ─────────────────────────────────────────────────────────────
    public Result showSuns(Matcher matcher) {
        StringBuilder sb = new StringBuilder("Dropped Suns List:\n");
        int count = 0;
        for (TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof Sun) {
                Sun sun = (Sun) entity;
                if (!sun.isDone()) { //[cite: 2]
                    
                    // فرمت: Type | Position: (X, Y) | Sun Amount: Value | Expiry: Time
                    sb.append(String.format("- %s | Position: (%d, %d) | Sun Amount: %d | Expiry: %.1fs\n",
                            sun.getType(), //[cite: 2]
                            sun.getCol(), //[cite: 2]
                            sun.getLane(), //[cite: 2]
                            sun.getAmount(), //[cite: 2]
                            sun.getSecondsRemaining() //[cite: 2]
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
