package pvz.Controller.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Capabilities.PlantPlacer;
import pvz.Models.Games.Modes.NormalMode;
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
        StringBuilder result = new StringBuilder();
        List<PlantCard> cards = ((NormalMode) context.getMode()).getPlantCards();
            
        if (cards == null || cards.isEmpty()) {
            result.append("No plant cards available.");
        } else {
            result.append("=== SEED PACKETS ===\n");
            
            int COLUMNS_PER_ROW = 3;
            int cardWidth = 40;
        
            for (int i = 0; i < cards.size(); i++) {
                PlantCard ps = cards.get(i);
                
                String cardInfo = String.format("- %s | Cost:%d | Lvl:%d | Cooldown:%.1f%s",
                        ps.getPlant().getType(),
                        ps.getCost(),
                        ps.getPlant().getLevel(),
                        ps.getCooldown(),
                        ps.getPlant().isBoosted() ? " | ⚡B" : "" 
                );
            
                result.append(String.format("%-" + cardWidth + "s", cardInfo));
            
                if ((i + 1) % COLUMNS_PER_ROW == 0 || i == cards.size() - 1) {
                    result.append("\n");
                } else {
                    result.append("   ");
                }
            }
        }
        return new Result(result.toString());
    }

    public Result plant(Matcher matcher) {
        String typeString = matcher.group("plantType");
        int x = Integer.parseInt(matcher.group("plantX"));
        int y = Integer.parseInt(matcher.group("plantY"));

        if(context.getMode() instanceof PlantPlacer mode){
            mode.handlePlacement(context, x, y, mode.findCard(typeString));
            return new Result("Planted");
        }else{
            return new Result("You cant ahs;odfj;oajsf");
        }
       
        
        // // Validation
        // if (x < 0 || x >= context.getMap().getColumns() || y < 0 || y >= context.getMap().getRows()) {
        //      return new Result("Invalid coordinates.");
        // }
        
        // Tile tile = context.getMap().getTile(x,y);
        // if (!tile.getPlants().isEmpty()) {
        //     return new Result("Tile already has a plant.");
        // }
        
        // // Find plant storage
        // PlantCard card = null;
        // for (PlantCard ps : ((NormalMode)context.getMode()).getPlantCards()) {
        //     if (ps.getPlant().getType().toString().equalsIgnoreCase(typeString)) {
        //         card = ps;
        //         break;
        //     }
        // }

        // if (card == null) {
        //     return new Result("Invalid plant type: " + typeString);
        // }

        // if (card.getCooldown() > 0.01f) {
        //     String message = String.format("This seed packet is recharging! Please wait %.1fs.", card.getCooldown()/10);
        //     return new Result(message);
        // }

        // PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(card.getPlant().getType());
        // // Check sun
        // if (!context.spendSun(sheet.getSunCost())) {
        //     return new Result("Not enough sun.");
        // }
        
        // // Create plant
        // Plant plant = new PlantFactory().create(card.getPlant().getType(), x, y, card.getPlant().getLevel() ,card.getPlant().isBoosted(), context);

        // context.spawnPlant(plant);
        // card.use();
        
        // return new Result(typeString + " placed at (" + x + ", " + y + ").");
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
