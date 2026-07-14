package pvz.Controller.Game;

import pvz.Models.Constants;
import pvz.Models.DataTypes.Vector2;
import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Enums.PlantStorage;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.GameSession;
import pvz.Models.Seasons.Levels.GameMap;
import pvz.Models.Seasons.Levels.NormalLevel;
import pvz.Models.Seasons.Levels.Wave;
import pvz.Models.Seasons.Season;
import pvz.View.Game.NormalGameMenu;
import pvz.View.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class PreNormalGameController extends PreGameController{
    private static final int MAX_PLANTS = 7;
    List<Plant> selectedPlants;
    int levelNumber;

    public PreNormalGameController(Season season, int level){
        super(season,level);
        selectedPlants=new ArrayList<>();
        levelNumber=level;
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
        for (Plant p: GameSession.getInstance().getCurrentUser().getProfile().getCollection().getUnlockedPlants()){
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
        for (PlantStorage ps: PlantStorage.values()){
            if (ps.getType().toString().equals(type)){
                plantType = ps.getType();
                break;
            }
        }
        if (plantType == null){
            return new Result("Plant not found.");
        }

        List<Plant> unlocked = GameSession.getInstance().getCurrentUser().getProfile().getCollection().getUnlockedPlants();
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

        for (PlantStorage ps: PlantStorage.values()){
            if (ps.getType() == plantType){
                selectedPlants.add(ps.getCopy(new Vector2(-1,-1)));
                break;
            }
        }

        StringBuilder output=new StringBuilder("Plant added. Selected Plants (" + selectedPlants.size() + "/" + MAX_PLANTS + "):");
        for (Plant p : selectedPlants){
            output.append("\n- ").append(p.getType().toString());
        }
        return new Result(output.toString());
    }

    public Result plantRemove(Matcher matcher){
        String type=matcher.group("type").trim().toUpperCase();

        PlantType plantType = null;
        for (PlantStorage ps: PlantStorage.values()){
            if (ps.getType().toString().equals(type)){
                plantType = ps.getType();
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
        for (PlantStorage ps: PlantStorage.values()){
            if (ps.getType().toString().equals(type)){
                plantType = ps.getType();
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

    @Override
    public Result startGame(Matcher matcher) {
        if (selectedPlants.size() < MAX_PLANTS){
            return new Result("You must select all " + MAX_PLANTS + " plants before starting. Currently selected: " + selectedPlants.size() + "/" + MAX_PLANTS);
        }

        StringBuilder output=new StringBuilder("Starting game with:");
        for (Plant p : selectedPlants){
            String boost = p.isBoosted() ? " [BOOSTED]" : "";
            output.append("\n- ").append(p.getType().toString()).append(boost);
        }
        List<Wave> waves =new ArrayList<>();
        waves.add(new Wave(1,false,20,new ArrayList<>()));
        waves.getFirst().getSpawns().add(new ZombieSpawnEntry());
        NormalLevel level=new NormalLevel(
                new GameEngine(),
                new GameMap(Constants.DEFAULT_ROWS,Constants.DEFAULT_COLS),
                levelNumber,Constants.DEFAULT_INITIAL_SUN,

                )
        return new Result(output.toString(), new NormalGameMenu(selectedPlants));
    }
}
