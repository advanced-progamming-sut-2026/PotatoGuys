package pvz.Controller;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.AppContext;
import pvz.View.GameMenu;
import pvz.View.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class PreGameController {
    private static final int MAX_PLANTS = 7;
    private final PlantFactory plantFactory = new PlantFactory();
    List<Plant> selectedPlants;

    public PreGameController(){
        selectedPlants=new ArrayList<>();
    }

    public Result showAllPlants(Matcher matcher){
        StringBuilder output=new StringBuilder();
        for (PlantType type : PlantType.values()){
            var sheet = PlantRegistry.getInstance().getSheet(type);
            if (sheet == null) continue;
            output.append("type: ").append(type.toString());
            output.append("\n Sun Cost: ").append(sheet.getSunCost());
        }
        return new Result(output.toString());
    }

    public Result showAvailablePlants(Matcher matcher){
        StringBuilder output=new StringBuilder();
        for (Plant p:AppContext.getInstance().getCurrentUser().getProfile().getCollection().getUnlockedPlants()){
            output.append("type: ").append(p.getType().toString());
            output.append("\n Sun Cost: ").append(p.getSunCost());
        }
        return new Result(output.toString());
    }

    public Result plantAdd(Matcher matcher){
        String type=matcher.group("type").trim().toUpperCase();

        if (selectedPlants.size() >= MAX_PLANTS){
            return new Result("You can only select up to " + MAX_PLANTS + " plants.");
        }

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()){
            if (pt.toString().equals(type)){
                plantType = pt;
                break;
            }
        }
        if (plantType == null){
            return new Result("Plant not found.");
        }

        List<Plant> unlocked = AppContext.getInstance().getCurrentUser().getProfile().getCollection().getUnlockedPlants();
        boolean isUnlocked = false;
        for (Plant p : unlocked){
            if (p.getType() == plantType){
                isUnlocked = true;
                break;
            }
        }
        if (!isUnlocked){
            return new Result("You have not unlocked this plant.");
        }

        for (Plant p : selectedPlants){
            if (p.getType() == plantType){
                return new Result("This plant is already in your selection.");
            }
        }

        Plant owned = AppContext.getInstance().getCurrentUser().getProfile().getCollection().getPlant(plantType);
        selectedPlants.add(owned != null ? plantFactory.copyUnplaced(owned) : plantFactory.createUnplaced(plantType, 1, false));

        StringBuilder output=new StringBuilder("Plant added. Selected Plants (" + selectedPlants.size() + "/" + MAX_PLANTS + "):");
        for (Plant p : selectedPlants){
            output.append("\n- ").append(p.getType().toString());
        }
        return new Result(output.toString());
    }

    public Result plantRemove(Matcher matcher){
        String type=matcher.group("type").trim().toUpperCase();

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()){
            if (pt.toString().equals(type)){
                plantType = pt;
                break;
            }
        }
        if (plantType == null){
            return new Result("Plant not found.");
        }

        for (int i = 0; i < selectedPlants.size(); i++){
            if (selectedPlants.get(i).getType() == plantType){
                selectedPlants.remove(i);
                StringBuilder output=new StringBuilder("Plant removed. Selected Plants (" + selectedPlants.size() + "/" + MAX_PLANTS + "):");
                for (Plant p : selectedPlants){
                    output.append("\n- ").append(p.getType().toString());
                }
                return new Result(output.toString());
            }
        }
        return new Result("This plant is not in your selection.");
    }

    public Result boostPlant(Matcher matcher){
        String type=matcher.group("type").trim().toUpperCase();

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()){
            if (pt.toString().equals(type)){
                plantType = pt;
                break;
            }
        }
        if (plantType == null){
            return new Result("Plant not found.");
        }

        for (Plant p : selectedPlants){
            if (p.getType() == plantType){
                if (p.isBoosted()){
                    return new Result("This plant is already boosted.");
                }
                p.setBoosted(true);
                return new Result(p.getType().toString() + " has been boosted.");
            }
        }
        return new Result("This plant is not in your selection.");
    }

    public Result startGame(Matcher matcher){
        if (selectedPlants.size() < MAX_PLANTS){
            return new Result("You must select all " + MAX_PLANTS + " plants before starting. Currently selected: " + selectedPlants.size() + "/" + MAX_PLANTS);
        }

        StringBuilder output=new StringBuilder("Starting game with:");
        for (Plant p : selectedPlants){
            String boost = p.isBoosted() ? " [BOOSTED]" : "";
            output.append("\n- ").append(p.getType().toString()).append(boost);
        }
        return new Result(output.toString(), new GameMenu(selectedPlants));
    }
}
