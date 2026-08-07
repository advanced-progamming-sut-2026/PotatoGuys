package com.pvz.models.engine;

import java.util.ArrayList;
import java.util.List;

import com.pvz.PvZ2;
import com.pvz.models.AppContext;

public class GameEngine {
    public static GameEngine instance;

    public static GameEngine getInstance(){
        if (instance == null){
            instance = new GameEngine();
        }
        return instance;
    }
    private List<TickAware> entities = new ArrayList<>();
    private List<TickAware> toAdd = new ArrayList<>();
    private List<TickAware> toRemove = new ArrayList<>();
    private boolean firstTickDone = false;

    public void register(TickAware entity){
        getToAdd().add(entity);
    }

    public void unRegister(TickAware entity){
        getToRemove().add(entity);
    }

    public void update(float dt) {
        if(AppContext.getInstance().getGameContext().isGameOver()){
            return;
        }
        for (TickAware entity : new ArrayList<>(getToAdd())) {
            entity.enter();
        }
        getEntities().addAll(getToAdd());
        getToAdd().clear();

        getEntities().removeAll(getToRemove());
        getToRemove().clear();

        List<TickAware> snapshot = new ArrayList<>(getEntities());

        // Updating all entities
        for (TickAware entity : snapshot) {
            entity.update(dt);
        }

        // Rendering all entities
        PvZ2.batch.begin();
        for (TickAware entity: snapshot){
            entity.draw();
        }
        PvZ2.batch.end();

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
