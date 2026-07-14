package pvz.Controller.Game;

import pvz.Models.Seasons.Levels.Level;
import pvz.Models.Seasons.Levels.NormalLevel;
import pvz.View.Result;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Engine.GameEngine;
import pvz.Models.Engine.TickAware;
import pvz.Models.Seasons.Levels.Tile;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.Enums.PlantStorage;
import pvz.Models.DataTypes.Vector2;

import java.util.regex.Matcher;

public class NormalGameController extends GameController{
    public NormalGameController(NormalLevel level) {
        super(level);
    }

    public Result collectSun(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("sunX"));
        int y = Integer.parseInt(matcher.group("sunY"));
        
        GameEngine engine = GameEngine.getInstance();
        for (TickAware entity : engine.getEntities()) {
            if (entity instanceof Sun) {
                Sun sun = (Sun) entity;
                // Assuming coordinates match within a reasonable threshold
                if (Math.abs(sun.getPosition().x - x) < 1.0 && Math.abs(sun.getPosition().y - y) < 1.0) {
                    level.addSun(sun.getType().getAmountSun());
                    sun.dispose();
                    return new Result("Sun collected!");
                }
            }
        }
        return new Result("No sun found at these coordinates.");
    }

    public Result showSun(Matcher matcher) {
        return new Result("Current Sun: " + level.getCurrentSun());
    }

    public Result cheatSun(Matcher matcher) {
        int amount = Integer.parseInt(matcher.group("sunCount"));
        level.addSun(amount);
        return new Result("Added " + amount + " sun.");
    }

    public Result plant(Matcher matcher) {
        String typeString = matcher.group("plantType");
        int x = Integer.parseInt(matcher.group("plantX"));
        int y = Integer.parseInt(matcher.group("plantY"));
        
        // Validation
        if (x < 0 || x >= level.getGameMap().getColumns() || y < 0 || y >= level.getGameMap().getRows()) {
             return new Result("Invalid coordinates.");
        }
        
        Tile tile = level.getGameMap().getMap()[y][x];
        if (tile.getPlant() != null) {
            return new Result("Tile already has a plant.");
        }
        
        // Find plant storage
        PlantStorage storage = null;
        for (PlantStorage ps : PlantStorage.values()) {
            if (ps.getType().toString().equalsIgnoreCase(typeString)) {
                storage = ps;
                break;
            }
        }
        if (storage == null) {
            return new Result("Invalid plant type: " + typeString);
        }
        
        // Check sun
        if (!level.spendSun(storage.getSunCost())) {
            return new Result("Not enough sun.");
        }
        
        // Create plant
        Plant plant = storage.getCopy(new Vector2(x, y));
        tile.setPlant(plant);
        level.getEngine().register(plant);
        
        return new Result(typeString + " placed at (" + x + ", " + y + ").");
    }

    public Result pluckPlant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("pluckX"));
        int y = Integer.parseInt(matcher.group("pluckY"));
        
        // Validation
        if (x < 0 || x >= level.getGameMap().getColumns() || y < 0 || y >= level.getGameMap().getRows()) {
             return new Result("Invalid coordinates.");
        }
        
        Tile tile = level.getGameMap().getMap()[y][x];
        if (tile.getPlant() == null) {
            return new Result("No plant to pluck at (" + x + ", " + y + ").");
        }
        
        Plant plant = tile.getPlant();
        level.getEngine().unRegister(plant);
        tile.setPlant(null);
        
        return new Result("Plant plucked from (" + x + ", " + y + ").");
    }
}
