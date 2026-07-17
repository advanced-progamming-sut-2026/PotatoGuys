package pvz.Controller.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.Modes.GameMode;
import pvz.Models.Games.Modes.NormalMode;
import pvz.Models.Games.card.Card;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.Games.map.Tile;
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

    public Result showCards(Matcher matcher){
        return new Result(context.getMode().getCardsStatus(context));
    }

    public Result plant(Matcher matcher) {
        String plantType = matcher.group("plantType");
        int col = Integer.parseInt(matcher.group("plantX"));
        int lane = Integer.parseInt(matcher.group("plantY"));

        GameMode mode = context.getMode();
        if (mode instanceof PlantPlacer placer) {
            Card card = placer.findCard(context,plantType);
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

    public Result pluckPlant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group("pluckX"));
        int y = Integer.parseInt(matcher.group("pluckY"));
        
        // Validation
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
}
