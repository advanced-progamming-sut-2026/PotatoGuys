package pvz.Controller;

import java.util.regex.Matcher;

import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.Result;

public class NetworkController {
    public Result connect(Matcher matcher) { return null; }
    public Result back(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
