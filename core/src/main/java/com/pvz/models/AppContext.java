package com.pvz.models;

import com.pvz.models.games.GameContext;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.user.User;

public class AppContext {
    private static AppContext instance;

    private User currentUser;
    private GameContext gameContext;
    private GreenHouse greenHouse;

    private AppContext() { }

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public GreenHouse getGreenHouse() { return greenHouse; }
    public void setGreenHouse(GreenHouse greenHouse) { this.greenHouse = greenHouse; }

    public GameContext getGameContext() {return gameContext;}
    public void setGameContext(GameContext gameContext) {this.gameContext = gameContext;}
}
