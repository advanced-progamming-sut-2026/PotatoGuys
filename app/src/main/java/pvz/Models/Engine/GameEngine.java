package pvz.Models.Engine;

import java.util.ArrayList;
import java.util.List;

import pvz.App;
import pvz.Models.AppContext;

public class GameEngine {
    public static GameEngine instance;

    public static GameEngine getInstance(){
        if (instance==null){
            instance=new GameEngine();
        }
        return instance;
    }
    private List<TickAware> entities=new ArrayList<>();
    private List<TickAware> toAdd = new ArrayList<>();
    private List<TickAware> toRemove = new ArrayList<>();
    private boolean firstTickDone = false;

    public void register(TickAware entity){
        getToAdd().add(entity);
    }

    public void unRegister(TickAware entity){
        getToRemove().add(entity);
    }

    public void advanceTime(int ticks){
        for (int i = 0; i < ticks; i++) {
            processOneTick();
            if(AppContext.getInstance().getGameContext().isGameOver()){
                break;
            }
        }
    }

    private void processOneTick() {
        for (TickAware entity : new ArrayList<>(getToAdd())) {
            entity.enter();
        }
        getEntities().addAll(getToAdd());
        getToAdd().clear();

        getEntities().removeAll(getToRemove());
        getToRemove().clear();

        List<TickAware> snapshot = new ArrayList<>(getEntities());
        for (TickAware entity : snapshot) {
            entity.update();
        }
        firstTickDone = true;
    }

    public List<TickAware> getEntities() {
        return entities;
    }

    public List<TickAware> getToAdd() {
        return toAdd;
    }

    public List<TickAware> getToRemove() {
        return toRemove;
    }

    public boolean isFirstTickDone() {
        return firstTickDone;
    }

    public void reset() {
        entities.clear();
        toAdd.clear();
        toRemove.clear();
        firstTickDone = false;
    }
}
