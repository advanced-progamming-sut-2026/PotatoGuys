package pvz;

import java.util.HashMap;

import pvz.models.AppContext;
import pvz.models.user.User;
import pvz.utils.SaveManager;
import pvz.view.MainMenu;
import pvz.view.MenuManager;
import pvz.view.RegisterMenu;

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
                        user.refreshQuestLog();
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
