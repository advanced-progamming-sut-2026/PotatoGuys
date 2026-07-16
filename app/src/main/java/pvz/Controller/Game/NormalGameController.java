package pvz.Controller.Game;

import java.util.ArrayList;
import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.Engine.GameEngine;
import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.map.Tile;
import pvz.Models.User.MyPlant;
import pvz.View.Result;

public class NormalGameController extends GameController{
    public NormalGameController(GameContext context) {
        super(context);
    }

    public Result collectSun(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("sunX"));
        int y = Integer.parseInt(matcher.group("sunY"));
        
        for (Sun sun : new ArrayList<>(context.getSuns())) {
            if (sun.getCol() == x && sun.getLane() == y && !sun.isDone()) {
                sun.collect(context);
                context.removeSun(sun);
                return new Result("Sun collected!");
            }
        }
        return new Result("No sun found at these coordinates.");
    }

    public Result showSun(Matcher matcher) {
        return new Result("Current Sun: " + context.getCurrentSun());
    }

    public Result cheatSun(Matcher matcher) {
        int amount = Integer.parseInt(matcher.group("sunCount"));
        context.addSun(amount);
        return new Result("Added " + amount + " sun.");
    }

    public Result plant(Matcher matcher) {
        String typeString = matcher.group("plantType");
        int x = Integer.parseInt(matcher.group("plantX"));
        int y = Integer.parseInt(matcher.group("plantY"));
        
        // Validation
        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
             return new Result("Invalid coordinates.");
        }
        
        Tile tile = context.getMap().getTile(y,x);
        if (!tile.getPlants().isEmpty()) {
            return new Result("Tile already has a plant.");
        }
        
        // Find plant storage
        MyPlant type = null;
        for (MyPlant ps : AppContext.getInstance().getCurrentUser().getProfile().getCollection().getUnlockedPlants()) {
            if (ps.getType().toString().equalsIgnoreCase(typeString)) {
                type = ps;
                break;
            }
        }
        if (type == null) {
            return new Result("Invalid plant type: " + typeString);
        }

        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(type.getType());
        // Check sun
        if (!context.spendSun(sheet.getSunCost())) {
            return new Result("Not enough sun.");
        }
        
        // Create plant
        Plant plant = new PlantFactory().create(type.getType(), x, y, context);
        context.spawnPlant(plant);
        
        return new Result(typeString + " placed at (" + x + ", " + y + ").");
    }

    public Result pluckPlant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("pluckX"));
        int y = Integer.parseInt(matcher.group("pluckY"));
        
        // Validation
        if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
             return new Result("Invalid coordinates.");
        }
        
        Tile tile = context.getMap().getTile(y, x);
        if (tile.getPlants().isEmpty()) {
            return new Result("No plant to pluck at (" + x + ", " + y + ").");
        }
        
        tile.getPlants().forEach(p -> {
            context.removePlant(p);
        });
        tile.getPlants().clear();
        
        return new Result("Plant plucked from (" + x + ", " + y + ").");
    }
}
