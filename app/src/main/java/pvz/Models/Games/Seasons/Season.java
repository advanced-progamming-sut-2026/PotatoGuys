package pvz.Models.Games.Seasons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.Levels.Level;

public class Season {
    private String name;
    private List<Level> levels;
    private Map<Integer,Boolean> levelUnlocked;

    public Season(String name){
        this.name=name;
        this.levels=new ArrayList<>();
        this.levelUnlocked=new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public List<Level> getLevels() {
        return levels;
    }

}
