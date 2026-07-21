package pvz.controller;

import java.util.regex.Matcher;

import pvz.view.MainMenu;
import pvz.view.Menu;
import pvz.view.Result;

public class NetworkController {
    public Result connect(Matcher matcher) { return null; }
    public Result back(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
