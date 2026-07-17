package pvz.Controller.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Levels.NormalLevel;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Levels.WavePhase;
import pvz.Models.Games.Modes.NormalMode;
import pvz.Models.Games.Seasons.Season;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.Games.map.GameMap;
import pvz.Models.User.MyPlant;
import pvz.View.Result;
import pvz.View.Game.NormalGameMenu;

public class PreNormalGameController extends PreGameController {
    private static final int MAX_PLANTS = 7;
    private List<PlantCard> selectedPlants;
    private int levelNumber;

    public PreNormalGameController(Season season, int level){
        super(season,level);
        selectedPlants=new ArrayList<>();
        levelNumber=level;
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
        for (MyPlant p : AppContext.getInstance().getCurrentUser().getProfile().getCollection().getUnlockedPlants()){
            output.append("\n- ").append(p.getType());
            output.append(" | Level: ").append(p.getLevel());
            output.append(" | Sun Cost: ").append(PlantRegistry.getInstance().getSheet(p.getType()).getSunCost());
            if (p.isBoosted()) output.append(" [BOOSTED]");
        }
        return new Result(output.toString());
    }

    public Result plantAdd(Matcher matcher){
        String type = matcher.group("type").trim();

        if (selectedPlants.size() >= MAX_PLANTS){
            return new Result("You can only select up to " + MAX_PLANTS + " plants.");
        }

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()){
            if (pt.toString().equalsIgnoreCase(type)){
                plantType = pt;
                break;
            }
        }
        if (plantType == null){
            return new Result("Plant not found.");
        }

        MyPlant owned = AppContext.getInstance().getCurrentUser().getProfile().getCollection().getPlant(plantType);
        if (owned == null){
            return new Result("You have not unlocked this plant.");
        }

        for (PlantCard p : selectedPlants){
            if (p.getPlant().getType() == plantType){
                return new Result("This plant is already in your selection.");
            }
        }

        PlantPropertySheet propertySheet = PlantRegistry.getInstance().getSheet(plantType);
        selectedPlants.add(new PlantCard(owned, propertySheet.getSunCost(), propertySheet.getRechargeSeconds()));

        StringBuilder output = new StringBuilder("Plant added. Selected Plants (" + selectedPlants.size() + "/" + MAX_PLANTS + "):");
        for (PlantCard p : selectedPlants){
            output.append("\n- ").append(p.getPlant().getType().toString()).append(" level:").append(p.getPlant().getLevel()).append(" boosted:").append(p.getPlant().isBoosted());
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
            if (selectedPlants.get(i).getPlant().getType() == plantType){
                selectedPlants.remove(i);
                StringBuilder output=new StringBuilder("Plant removed. Selected Plants (" + selectedPlants.size() + "/" + MAX_PLANTS + "):");
                for (PlantCard p : selectedPlants){
                    output.append("\n- ").append(p.getPlant().getType().toString());
                }
                return new Result(output.toString());
            }
        }
        return new Result("This plant is not in your selection.");
    }

    public Result boostPlant(Matcher matcher){
        String type = matcher.group("type").trim().toUpperCase();

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

        for (PlantCard p : selectedPlants){
            if (p.getPlant().getType() == plantType){
                if (p.getPlant().isBoosted()){
                    return new Result("This plant is already boosted.");
                }
                p.getPlant().setBoosted(true);
                return new Result(p.getPlant().getType().toString() + " has been boosted.");
            }
        }
        return new Result("This plant is not in your selection.");
    }

    @Override
    public Result startGame(Matcher matcher) {
        if (selectedPlants.isEmpty()){
            return new Result("You must select at least one plant to start!");
        }

        GameEngine engine = GameEngine.getInstance();
        engine.reset();

        GameMap map = new GameMap();

        List<Wave> waves = createHardcodedWaves(map);

        NormalLevel level = new NormalLevel(map, levelNumber, LevelType.NORMAL, 200 , waves);

        StringBuilder output=new StringBuilder("Starting game with:");
        for (PlantCard p : selectedPlants){
            String boost = p.getPlant().isBoosted() ? " [BOOSTED]" : "";
            output.append("\n- ").append(p.getPlant().getType().toString()).append(boost);
        }
        GameContext context = new GameContext(level);
        selectedPlants.forEach(context::addCard);
        AppContext.getInstance().setGameContext(context);

        return new Result(output.toString(), new NormalGameMenu(context));
    }

    private List<Wave> createHardcodedWaves(GameMap map) {
        List<Wave> waves = new ArrayList<>();
        int lanes = map.getRows();

        List<ZombieType> basicOnly = List.of(ZombieType.BASIC);
        List<ZombieType> basicAndMummy = List.of(ZombieType.BASIC, ZombieType.MUMMY);
        List<ZombieType> basicConeMummy = List.of(ZombieType.BASIC, ZombieType.CONEHEAD, ZombieType.MUMMY);

        WavePhase phase1 = new WavePhase(3, 50, basicOnly, false);
        waves.add(new Wave(1, false, 300, List.of(phase1), lanes, 3));

        WavePhase phase2a = new WavePhase(4, 40, basicAndMummy, false);
        WavePhase phase2b = new WavePhase(3, 25, basicAndMummy, true);
        waves.add(new Wave(2, false, 500, List.of(phase2a, phase2b), lanes, 3));

        WavePhase phase3a = new WavePhase(5, 35, basicConeMummy, false);
        WavePhase phase3b = new WavePhase(6, 20, basicConeMummy, true);
        waves.add(new Wave(3, true, 800, List.of(phase3a, phase3b), lanes, 3));

        return waves;
    }
}
