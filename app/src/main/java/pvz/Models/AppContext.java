package pvz.Models;

import pvz.Models.Games.GameContext;
import pvz.Models.User.User;

public class AppContext {
    private static AppContext instance;
    
    private User currentUser;
    private GameContext gameContext;

    private AppContext() { }
    
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public GameContext getGameContext() {return gameContext;}
    public void setGameContext(GameContext gameContext) {this.gameContext = gameContext;}
}