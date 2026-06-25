package Controller;

import View.MainMenu;
import View.Menu;
import View.Result;
import java.util.regex.Matcher;

public class ProfileController {
    public Result changeUsername(Matcher matcher) { return null; }
    public Result changeNickname(Matcher matcher) { return null; }
    public Result changeEmail(Matcher matcher) { return null; }
    public Result changePassword(Matcher matcher) { return null; }
    public Result showInfo(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
