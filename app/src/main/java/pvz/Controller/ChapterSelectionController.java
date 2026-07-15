package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Models.GreenHouse.GreenHouse;
import pvz.View.ChapterMenu;
import pvz.View.ChapterSelectionMenu;
import pvz.View.CollectionMenu;
import pvz.View.GreenHouseMenu;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.Result;

public class ChapterSelectionController {
    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "collection":
                nextMenu = new CollectionMenu();
                break;
            default:
                nextMenu = new ChapterSelectionMenu();
                return new Result("You cannot enter this menu." , nextMenu);
        }
        
        return new Result("Entered " + nextMenu.getName() , nextMenu);
    }
    public Result enterChapter(Matcher matcher)
    {
        String chapter=matcher.group("chapterName").trim().toLowerCase();
        return switch (chapter) {
            case "ancient egypt" -> new Result("", new ChapterMenu("Ancient Egypt"));
            case "frostbite caves" -> new Result("", new ChapterMenu("Frostbite Caves"));
            case "dark ages" -> new Result("", new ChapterMenu("Dark Ages"));
            case "big wave beach" -> new Result("", new ChapterMenu("Big Wave Beach"));
            default -> new Result("Invalid chapter name");
        };
    }
    public Result travelLog(Matcher matcher) { return null; }
    public Result leaderboard(Matcher matcher) { return null; }
    public Result coinWallet(Matcher matcher) { return null; }
    public Result gemWallet(Matcher matcher) { return null; }
    public Result cheatAdd(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
