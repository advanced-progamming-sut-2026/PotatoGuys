package pvz.Models;

import pvz.Models.User.User;

public class GameSession {
    private static GameSession instance;
    
    private User currentUser;
    
    private GameSession() { }
    
    public static GameSession getInstance() {
        if (instance == null) {
            instance = new GameSession();
        }
        return instance;
    }
    
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
}