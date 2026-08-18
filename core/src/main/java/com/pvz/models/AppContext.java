package com.pvz.models;

import com.pvz.models.games.GameContext;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.user.User;

public class AppContext {
    private static AppContext instance;

    private User currentUser;
    private GameContext gameContext;
    private GreenHouse greenHouse;
    private MatchSession matchSession;

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

    /** Set by OpponentSelectMenu right before launching an online I,Zombie match.
     *  Null outside of that flow — always check for null before reading it. */
    public MatchSession getMatchSession() { return matchSession; }
    public void setMatchSession(MatchSession matchSession) { this.matchSession = matchSession; }
}
