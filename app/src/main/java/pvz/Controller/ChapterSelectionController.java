package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.View.*;

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
            case "ancient egypt" -> new Result("Season selected: Ancient Egypt", new ChapterMenu("Ancient Egypt"));
            case "frostbite caves" -> new Result("Season selected: Frostbite Caves", new ChapterMenu("Frostbite Caves"));
            case "dark ages" -> new Result("Season selected: Dark Ages", new ChapterMenu("Dark Ages"));
            case "big wave beach" -> new Result("Season selected: Big Wave Beach", new ChapterMenu("Big Wave Beach"));
            default -> new Result("Invalid chapter name");
        };
    }

    public Result travelLog(Matcher matcher) {
        return new Result("Entering Travel Log (Quest Menu) ...", new QuestMenu());
    }

    public Result leaderboard(Matcher matcher) { return null; }

    public Result coinWallet(Matcher matcher) {
        return new Result("Coins: "+ AppContext.getInstance().getCurrentUser().getProfile().getCoins());
    }

    public Result gemWallet(Matcher matcher) {
        return new Result("Gems: "+ AppContext.getInstance().getCurrentUser().getProfile().getDiamonds());
    }

    public Result cheatAdd(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }

    public Result greenhouse(Matcher matcher) {
        return new Result("Entering Green House ...", new GreenHouseMenu());
    }
}
