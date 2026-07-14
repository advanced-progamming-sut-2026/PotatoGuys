package pvz.Controller.Game;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Enums.PlantStorage;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.GameSession;
import pvz.Models.Seasons.Levels.GameMap;
import pvz.Models.Seasons.Levels.LevelGameContext;
import pvz.Models.Seasons.Levels.NormalLevel;
import pvz.Models.Seasons.Levels.Wave;
import pvz.Models.Seasons.Levels.WavePhase;
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
        if (selectedPlants.isEmpty()){
            // Debugging mode: add some default plants if none selected
            for (PlantStorage ps: PlantStorage.values()){
                if (selectedPlants.size() < MAX_PLANTS){
                    selectedPlants.add(ps.getCopy(new Vector2(-1,-1)));
                } else {
                    break;
                }
            }
        }

        GameEngine engine = GameEngine.getInstance();
        engine.reset();

        GameMap map = new GameMap();
        LevelGameContext context = new LevelGameContext(engine, map, null);

        List<Wave> waves = createHardcodedWaves(context, map);

        NormalLevel level = new NormalLevel(engine, map, levelNumber, 150, waves, selectedPlants);
        
        // This is a bit of a hack, but we need to associate the level with the context if possible
        // Actually, LevelGameContext didn't have a setter for level. 
        // With the fix I applied to LevelGameContext, it should work fine without the level object.

        StringBuilder output=new StringBuilder("Starting game with:");
        for (Plant p : selectedPlants){
            String boost = p.isBoosted() ? " [BOOSTED]" : "";
            output.append("\n- ").append(p.getType().toString()).append(boost);
        }
        return new Result(output.toString(), new NormalGameMenu(level));
    }

    private List<Wave> createHardcodedWaves(LevelGameContext context, GameMap map) {
        List<Wave> waves = new ArrayList<>();
        int lanes = map.getRows();

        List<ZombieType> basicOnly = List.of(ZombieType.BASIC);
        List<ZombieType> basicAndMummy = List.of(ZombieType.BASIC, ZombieType.MUMMY);
        List<ZombieType> basicConeMummy = List.of(ZombieType.BASIC, ZombieType.CONEHEAD, ZombieType.MUMMY);

        WavePhase phase1 = new WavePhase(3, 50, basicOnly, false);
        waves.add(new Wave(1, false, 300, List.of(phase1), context, lanes, 3));

        WavePhase phase2a = new WavePhase(4, 40, basicAndMummy, false);
        WavePhase phase2b = new WavePhase(3, 25, basicAndMummy, true);
        waves.add(new Wave(2, false, 500, List.of(phase2a, phase2b), context, lanes, 3));

        WavePhase phase3a = new WavePhase(5, 35, basicConeMummy, false);
        WavePhase phase3b = new WavePhase(6, 20, basicConeMummy, true);
        waves.add(new Wave(3, true, 800, List.of(phase3a, phase3b), context, lanes, 3));

        return waves;
    }
}
