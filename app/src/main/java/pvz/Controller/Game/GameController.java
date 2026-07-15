package pvz.Controller.Game;

import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.map.Tile;
import pvz.View.Result;

public abstract class GameController {
    GameContext context;
    public GameController(GameContext context){
        this.context = context;
    }
    public Result advanceTime(Matcher matcher) {
        int ticks = Integer.parseInt(matcher.group("ticks"));
        context.getEngine().advanceTime(ticks);
        return new Result("Time advanced by " + ticks + " ticks.");
    }
    public Result releaseNuke(Matcher matcher) {
        int killed = 0;
        List<TickAware> entities = context.getEngine().getEntities();
        for (pvz.Models.Engine.TickAware entity : entities) {
            if (entity instanceof pvz.Models.Entities.Zombies.Zombie) {
                ((pvz.Models.Entities.Zombies.Zombie) entity).takeDamage(999999f, true);
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
        // StringBuilder mapOutput = new StringBuilder("Game Map:\n");
        // int rows = context.getMap().getRows();
        // int cols = context.getMap().getColumns();
        // for (int i = 0; i < rows; i++) {
        //     for (int j = 0; j < cols; j++) {
        //         Tile tile = context.getMap().getMap()[i][j];
        //         if (tile.getPlants() != null && tile.getPlants().get(tile.getPlants().size() - 1)!=null) {
        //             mapOutput.append("[P]"); // Represent plant
        //         } //else if (!tile.getZombies().isEmpty()) {
        //            // mapOutput.append("[Z]"); // Represent zombie
        //         //}
        //         else {
        //             mapOutput.append("[ ]"); // Represent empty
        //         }
        //     }
        //     mapOutput.append("\n");
        // }
        // return new Result(mapOutput.toString());
    }
    public Result showPlantsStatus(Matcher matcher) {
        StringBuilder status = new StringBuilder("Plants Status:\n");
        int rows = context.getMap().getRows();
        int cols = context.getMap().getColumns();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile tile = context.getMap().getMap()[i][j];
                Plant plant = tile.getPlants().get(tile.getPlants().size() - 1);
                if (plant != null) {
                    status.append("Plant at (").append(j).append(",").append(i).append("): ")
                          .append(plant.getType()).append(" | HP: ").append((int)plant.getHp()).append("\n");
                }
            }
        }
        if (status.length() == 15) { // "Plants Status:\n"
            return new Result("No plants on the map.");
        }
        return new Result(status.toString());
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
        for (pvz.Models.Engine.TickAware entity : context.getEngine().getEntities()) {
            if (entity instanceof pvz.Models.Entities.Zombies.Zombie) {
                pvz.Models.Entities.Zombies.Zombie zombie = (pvz.Models.Entities.Zombies.Zombie) entity;
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

}
