package pvz.Controller;

import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.Games.Seasons.Season;
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
    public Result enterChapter(Matcher matcher) {
        String chapter = matcher.group("chapterName").trim().toLowerCase();
        Season season = AppContext.getInstance().getCurrentUser().getProfile().getSeasonByName(chapter);
        if (season == null) return new Result("Invalid chapter name");
        else if (season.isLocked()) return new Result("This Chapter is locked!");
        else return new Result("Entering Chapter: " + season.getName() + " ...", new ChapterMenu(season.getName()));
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
