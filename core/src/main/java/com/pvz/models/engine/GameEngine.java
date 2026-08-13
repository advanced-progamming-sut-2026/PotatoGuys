package com.pvz.models.engine;

import java.util.ArrayList;
import java.util.List;

import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.models.games.GameContext;

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
        GameContext gameCtx = AppContext.getInstance().getGameContext();
        // Apply anything queued by the previous tick's collision before anyone runs.
        gameCtx.flushPending();

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

        // Detect overlaps now that every entity has settled into its new position
        CollisionSystem.getInstance().detect(gameCtx);
        // Collisions may have killed/destroyed entities — apply their queued
        // removals/additions now, safely outside of the detection loops.
        gameCtx.flushPending();

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
