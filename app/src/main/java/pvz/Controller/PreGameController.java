package pvz.Controller;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Entities.Plants.Enums.PlantStorage;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.GameSession;
import pvz.View.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class PreGameController {
    List<Plant> selectedPlants;

    public PreGameController(){
        selectedPlants=new ArrayList<>();
    }

    public Result showAllPlants(Matcher matcher){
        StringBuilder output=new StringBuilder();
        for (PlantStorage p: PlantStorage.values()){
            output.append("type: ").append(p.getType().toString());
            output.append("\n Sun Cost: ").append(p.getSunCost());
        }
        return new Result(output.toString());
    }

    public Result showAvailablePlants(Matcher matcher){
        StringBuilder output=new StringBuilder();
        for (Plant p:GameSession.getInstance().getCurrentUser().getProfile().getCollection().getUnlockedPlants()){
            output.append("type: ").append(p.getType().toString());
            output.append("\n Sun Cost: ").append(p.getSunCost());
        }
        return new Result(output.toString());
    }

    public Result plantAdd(Matcher matcher){
        String type=matcher.group("type").trim().toUpperCase();
        for (PlantStorage ps: PlantStorage.values()){
            if (ps.getType().toString().equals(type)){
                selectedPlants.add(ps.getCopy(new Vector2(-1,-1)));
                StringBuilder output=new StringBuilder("Selected Plants:");
                for (Plant p :selectedPlants){
                    output.append("\n").append(p.getType().toString());
                }
                return new Result(output.toString());
            }
        }
        return new Result("Plant not found");
    }

    public Result plantRemove(Matcher matcher){
        return null;
    }

    public Result boostPlant(Matcher matcher){
        return null;
    }

    public Result startGame(Matcher matcher){
        return null;
    }
}
