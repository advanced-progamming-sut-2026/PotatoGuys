package Controller;

import java.util.regex.Matcher;

import View.GameMenu;
import View.MainMenu;
import View.Menu;
import View.NetworkMenu;
import View.NewsMenu;
import View.ProfileMenu;
import View.RegisterMenu;
import View.Result;
import View.SettingsMenu;

public class MainController {
    public Result enterMenu(Matcher matcher) { 
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "game":
                nextMenu = new GameMenu();
                break;
            case "settings":
                nextMenu = new SettingsMenu();
                break;
            case "network":
                nextMenu = new NetworkMenu();
                break;
            case "news":
                nextMenu = new NewsMenu();
                break;
            case "profile":
                nextMenu = new ProfileMenu();
                break;
            default:
                nextMenu = new MainMenu();
                return new Result("You cannot enter this menu." , nextMenu);
        }
        
        return new Result("Enterned " + nextMenu.getName() , nextMenu);

    }
    public Result exit(Matcher matcher) {
        return new Result("Use 'menu logout' to exit from Main Menu.", new MainMenu());
    }
    public Result logout(Matcher matcher) { 
        return new Result("logouted", new RegisterMenu());
    }
}
