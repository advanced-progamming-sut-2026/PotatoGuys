package Controller;

import View.MainMenu;
import View.Menu;
import View.Result;
import java.util.regex.Matcher;

public class NewsController {
    public Result showUnread(Matcher matcher) { return null; }
    public Result showAll(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
