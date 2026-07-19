package pvz.Models.Games.Seasons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pvz.Models.Games.Levels.Level;

public class Season {
    private String name;
    private boolean locked;
    private List<Level> levels;
    private Map<Integer,Boolean> levelUnlocked;

    public Season(String name){
        this.name=name;
        this.levels=new ArrayList<>();
        this.levelUnlocked=new HashMap<>();
        locked=true;
    }

    public String getName() {
        return name;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void unlock(){
        locked=false;
    }

    public boolean isLocked(){
        return locked;
    }

    public boolean isLevelUnlocked(int levelNumber){
        if (levelUnlocked.containsKey(levelNumber)){
            return levelUnlocked.get(levelNumber);
        }
        return false;
    }

}
