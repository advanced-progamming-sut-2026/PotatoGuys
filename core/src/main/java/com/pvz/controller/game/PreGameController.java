package com.pvz.controller.game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.pvz.models.AppContext;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.data.PlantStatResolver.ResolvedStats;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.levels.Level;
import com.pvz.models.user.MyPlant;

public class PreGameController {
/*
    private static final int MAX_PLANTS = 7;
    private List<PlantCard> selectedPlants;
    private Level level;

    public PreGameController(Level level) {
        this.level = level;
        selectedPlants = new ArrayList<>();
    }

    public Result showAllPlants(Matcher matcher) {
        StringBuilder output = new StringBuilder();
        for (PlantType type : PlantType.values()) {
            var sheet = PlantConfigRegistry.getInstance().resolveSheet(type);
            if (sheet == null)
                continue;
            output.append("\n type: ").append(type.toString());
            output.append("\n Sun Cost: ").append(sheet.getSunCost());
        }
        return new Result(output.toString());
    }

    public Result showAvailablePlants(Matcher matcher) {
        StringBuilder output = new StringBuilder();
        List<MyPlant> unlockedPlants = AppContext.getInstance().getCurrentUser().getProfile().getCollection()
                .getUnlockedPlants();
        for (MyPlant p : unlockedPlants) {
            if (!level.isPlantAllowed(p.getType()))
                continue;
            output.append("\n- ").append(p.getType());
            output.append(" | Level: ").append(p.getLevel());
            output.append(" | Sun Cost: ").append(PlantConfigRegistry.getInstance().resolveSheet(p.getType()).getSunCost());
            if (p.isBoosted())
                output.append(" [BOOSTED]");
        }
        return new Result(output.toString());
    }

    public Result plantAdd(Matcher matcher) {
        String type = matcher.group("type").trim();

        if (selectedPlants.size() >= MAX_PLANTS) {
            return new Result("You can only select up to " + MAX_PLANTS + " plants.");
        }

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.toString().equalsIgnoreCase(type)) {
                plantType = pt;
                break;
            }
        }
        if (plantType == null) {
            return new Result("Invalid plant type.");
        }

        if (!level.isPlantAllowed(plantType)) {
            return new Result("This plant is not allowed in this game mode!");
        }

        MyPlant owned = AppContext.getInstance().getCurrentUser().getProfile().getCollection().getPlant(plantType);
        if (owned == null) {
            return new Result("You have not unlocked this plant.");
        }

        for (PlantCard p : selectedPlants) {
            if (p.getPlant().getType() == plantType) {
                return new Result("This plant is already in your selection.");
            }
        }

        PlantPropertySheet propertySheet = PlantConfigRegistry.getInstance().resolveSheet(plantType);
        ResolvedStats stats = PlantStatResolver.resolve(propertySheet, owned.getLevel());
        selectedPlants.add(new PlantCard(owned, stats.getSunCost(), stats.getRechargeSeconds()));

        StringBuilder output = new StringBuilder(
                "Plant added. Selected Plants (" + selectedPlants.size() + "/" + MAX_PLANTS + "):");
        for (PlantCard p : selectedPlants) {
            output.append("\n- ").append(p.getPlant().getType().toString()).append(" level:")
                    .append(p.getPlant().getLevel()).append(" boosted:").append(p.getPlant().isBoosted());
        }
        return new Result(output.toString());
    }

    public Result plantRemove(Matcher matcher) {
        String type = matcher.group("type").trim();

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.toString().equalsIgnoreCase(type)) {
                plantType = pt;
                break;
            }
        }
        if (plantType == null) {
            return new Result("Invalid plant type.");
        }

        for (int i = 0; i < selectedPlants.size(); i++) {
            if (selectedPlants.get(i).getPlant().getType() == plantType) {
                selectedPlants.remove(i);
                StringBuilder output = new StringBuilder(
                        "Plant removed. Selected Plants (" + selectedPlants.size() + "/" + MAX_PLANTS + "):");
                for (PlantCard p : selectedPlants) {
                    output.append("\n- ").append(p.getPlant().getType().toString());
                }
                return new Result(output.toString());
            }
        }
        return new Result("This plant is not in your selection.");
    }

    public Result boostPlant(Matcher matcher) {
        String type = matcher.group("type").trim();

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.toString().equalsIgnoreCase(type)) {
                plantType = pt;
                break;
            }
        }
        if (plantType == null) {
            return new Result("Invalid plant type.");
        }

        for (PlantCard p : selectedPlants) {
            if (p.getPlant().getType() == plantType) {
                if (p.getPlant().isBoosted()) {
                    return new Result("This plant is already boosted.");
                }
                p.getPlant().setBoosted(true);
                return new Result(p.getPlant().getType().toString() + " has been boosted.");
            }
        }
        return new Result("This plant is not in your selection.");
    }

    public Result startGame(Matcher matcher) {

        if (selectedPlants.isEmpty()) {
            return new Result("You must select at least one plant to start!");
        }

        GameEngine engine = GameEngine.getInstance();
        engine.reset();

        StringBuilder output = new StringBuilder("Starting game with:");
        for (PlantCard p : selectedPlants) {
            String boost = p.getPlant().isBoosted() ? " [BOOSTED]" : "";
            output.append("\n- ").append(p.getPlant().getType().toString()).append(boost);
        }
        GameContext context = new GameContext(level);
        selectedPlants.forEach(context::addCard);
        AppContext.getInstance().setGameContext(context);

        return new Result(output.toString(), new RunningGameMenu(new GameController(context)));
    }

*/
}
