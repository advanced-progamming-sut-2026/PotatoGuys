package pvz.Controller;

import java.util.regex.Matcher;

import pvz.View.LoginMenu;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.RegisterMenu;
import pvz.View.Result;

public class LoginController {
    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "main":
                nextMenu = new MainMenu();
                break;
            default:
                nextMenu = new LoginMenu();
                return new Result("You cannot enter this menu." , nextMenu);
        }
        
        return new Result("Enterned " + nextMenu.getName() , nextMenu);
    }

    public Result login(Matcher matcher) { return null; }
    public Result forgetPassword(Matcher matcher) { return null; }
    public Result answer(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new RegisterMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
