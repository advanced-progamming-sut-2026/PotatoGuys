package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Controller.Game.GameController;
import pvz.Models.AppContext;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelLoader;
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
import pvz.View.Game.PreGameMenu;
import pvz.View.Game.RunningGameMenu;
import pvz.View.Game.modals.ChapterSellectionModal;
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

    public Result selectGameMode(Matcher matcher){
        int mode = Integer.parseInt(matcher.group("mode"));
        switch (mode){
            case 1:
                return new Result("Entering Adventure Mode ...", new GameMenu(new ChapterSellectionModal()));
            default:
                return new Result("Invalid game mode");
        }

    }

    public Result selectChapter(Matcher matcher) {
        int chapterInt = Integer.parseInt(matcher.group("chapter"));
        String chapter = null;
        switch (chapterInt) {
            case 1:
                chapter = "Ancient Egypt";
                break;
            case 2:
                chapter = "Frostbite Caves";
                break;
            case 3:
                chapter = "Dark Ages";
                break;
            case 4:
                chapter = "Big Wave Beach";
                break;
            default:
                return new Result("Invalid chapter number!");
        }
        Season season = AppContext.getInstance().getCurrentUser().getProfile().getSeasonByName(chapter);
        if (season == null) return new Result("Invalid chapter name");
        else if (season.isLocked()) return new Result("This Chapter is locked!");
        else return new Result("Entering Chapter: " + season.getName() + " ...", new GameMenu(new SelectLevelModal(season.getName())));
    }

    public Result selectLevel(Matcher matcher , Season season) {
        int levelNumber = Integer.parseInt(matcher.group("level"));
        if (season == null) return new Result("Invalid chapter name");
        else if (season.isLocked()) return new Result("This Chapter is locked!");
        else if (!season.isLevelUnlocked(levelNumber)) return new Result("This Level is locked!");
        Level level = LevelLoader.loadLevel(season.getName(), levelNumber);
        if(level.hasPreGame())
            return new Result(new PreGameMenu(level));
        GameContext context = new GameContext(level);
        AppContext.getInstance().setGameContext(context);
        return new Result("Game started!" , new RunningGameMenu(new GameController(context)));
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
