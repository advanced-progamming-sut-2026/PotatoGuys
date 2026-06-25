package Controller;

import java.util.regex.Matcher;

import View.LoginMenu;
import View.Menu;
import View.RegisterMenu;
import View.Result;

public class RegisterController {
    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "login":
                nextMenu = new LoginMenu();
                break;
            default:
                nextMenu = new RegisterMenu();
                return new Result("You have to login!" , nextMenu);
        }
        return new Result("Enterned " + nextMenu.getName() , nextMenu);
    }

    public Result register(Matcher matcher) { return null; }
    public Result pickQuestion(Matcher matcher) { return null; }
    public Result enterLogin(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) { return null; }
}
