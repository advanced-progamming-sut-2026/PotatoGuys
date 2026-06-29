package pvz.Models.Engine;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
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
        }
    }

    private void processOneTick() {
        if (!isFirstTickDone()) {
            for (TickAware e : getToAdd()) {
                e.enter();
            }
            firstTickDone = true;
        }

        getEntities().addAll(getToAdd());
        getToAdd().clear();

        getEntities().removeAll(getToRemove());
        getToRemove().clear();

        List<TickAware> snapshot = new ArrayList<>(getEntities());
        for (TickAware e : snapshot) {
            e.update();
        }
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
}
