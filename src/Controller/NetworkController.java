package Controller;

import View.MainMenu;
import View.Menu;
import View.Result;
import java.util.regex.Matcher;

public class NetworkController {
    public Result connect(Matcher matcher) { return null; }
    public Result back(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
