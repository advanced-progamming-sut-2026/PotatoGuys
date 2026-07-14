package pvz.Models;

import pvz.Models.User.User;

public class AppContext {
    private static AppContext instance;
    
    private User currentUser;
    
    private AppContext() { }
    
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
}