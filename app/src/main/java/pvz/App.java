package pvz;

import java.util.HashMap;

import pvz.Models.AppContext;
import pvz.Models.User.User;
import pvz.Utils.SaveManager;
import pvz.View.LoginMenu;
import pvz.View.MainMenu;
import pvz.View.MenuManager;
import pvz.View.RegisterMenu;

public class App {
    public static void main(String[] args) {
        MenuManager menuManager = new MenuManager();

        String savedUsername = SaveManager.getInstance().load("session.json", String.class);
        if (savedUsername != null) {
            HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
            if (usernames != null) {
                String id = usernames.get(savedUsername);
                if (id != null) {
                    User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
                    if (user != null) {
                        AppContext.getInstance().setCurrentUser(user);
                        menuManager.setCurrentMenu(new MainMenu());
                        System.out.println("Welcome back, " + user.getNickName() + "!");
                        menuManager.handleInput(null);
                        return;
                    }
                }
            }
            SaveManager.getInstance().delete("session.json");
        }

        menuManager.setCurrentMenu(new RegisterMenu());
        menuManager.handleInput(null);
    }
}
