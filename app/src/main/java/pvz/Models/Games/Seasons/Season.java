package pvz.Models.Games.Seasons;

import java.util.HashMap;
import java.util.Map;

public class Season {
    private String name;
    private boolean locked;
    private Map<Integer,Boolean> levelUnlocked;

    public Season(String name){
        this.name = name;
        this.levelUnlocked = new HashMap<>();
        locked = true;
    }

    public String getName() {
        return name;
    }

    public void unlock(){
        locked = false;
    }

    public boolean isLocked(){
        return locked;
    }

    public void unlockLevel(int levelNumber){
        levelUnlocked.put(levelNumber,true);
    }

    public boolean isLevelUnlocked(int levelNumber){
        if (levelUnlocked.containsKey(levelNumber)){
            return levelUnlocked.get(levelNumber);
        }
        return false;
    }

}
