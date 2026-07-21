package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.Games.Seasons.Season;
import pvz.Models.User.User;
import pvz.View.CollectionMenu;
import pvz.View.GreenHouseMenu;
import pvz.View.LeaderBoardMenu;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.QuestMenu;
import pvz.View.Result;
import pvz.View.Game.GameMenu;
import pvz.View.Game.modals.SelectLevelModal;

public class GameMenuController {
    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "collection":
                nextMenu = new CollectionMenu();
                break;
            default:
                return new Result("You cannot enter this menu.");
        }
        
        return new Result("Entered " + nextMenu.getName() , nextMenu);
    }
    public Result enterChapter(Matcher matcher) {
        String chapter = matcher.group("chapterName").trim().toLowerCase();
        Season season = AppContext.getInstance().getCurrentUser().getProfile().getSeasonByName(chapter);
        if (season == null) return new Result("Invalid chapter name");
        else if (season.isLocked()) return new Result("This Chapter is locked!");
        else return new Result("Entering Chapter: " + season.getName() + " ...", new GameMenu(new SelectLevelModal(season.getName())));
    }

    public Result travelLog(Matcher matcher) {
        return new Result("Entering Travel Log (Quest Menu) ...", new QuestMenu());
    }

    public Result leaderboard(Matcher matcher) {
        return new Result("Entering Leaderboard ...", new LeaderBoardMenu());
    }

    public Result coinWallet(Matcher matcher) {
        return new Result("Coins: "+ AppContext.getInstance().getCurrentUser().getProfile().getCoins());
    }

    public Result gemWallet(Matcher matcher) {
        return new Result("Gems: "+ AppContext.getInstance().getCurrentUser().getProfile().getDiamonds());
    }

    public Result cheatAdd(Matcher matcher) {
        User user=AppContext.getInstance().getCurrentUser();
        String amountStr=matcher.group("amount").trim();
        String currency=matcher.group("currency");
        int amount;
        try {
            amount=Integer.parseInt(amountStr);
        } catch (Exception ex){
            return new Result("Invalid amount");
        }
        switch (currency){
            case "coin"-> user.getProfile().addCoins(amount);
            case "diamond"-> user.getProfile().addDiamonds(amount);
            default -> {
                return new Result("Invalid Currency");
            }
        }
        user.saveUser();
        return new Result("Added "+amount+" "+currency+"s to current user");
    }


    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }

    public Result greenhouse(Matcher matcher) {
        return new Result("Entering Green House ...", new GreenHouseMenu());
    }
}
