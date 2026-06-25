package Controller;

import java.util.regex.Matcher;

import View.CollectionMenu;
import View.GameMenu;
import View.Menu;
import View.Result;

public class GameController {
    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "collection":
                nextMenu = new CollectionMenu();
                break;
            default:
                nextMenu = new GameMenu();
                return new Result("You cannot enter this menu." , nextMenu);
        }
        
        return new Result("Enterned " + nextMenu.getName() , nextMenu);
    }

    public Result enterChapter(Matcher matcher) { return null; }
    public Result greenhouse(Matcher matcher) { return null; }
    public Result travelLog(Matcher matcher) { return null; }
    public Result leaderboard(Matcher matcher) { return null; }
    public Result coinWallet(Matcher matcher) { return null; }
    public Result gemWallet(Matcher matcher) { return null; }
    public Result cheatAdd(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) { return null; }
}
