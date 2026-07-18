package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Utils.SaveManager;
import pvz.View.*;

public class MainController {
    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "game":
                nextMenu = new GameModesMenu();
                break;
            case "settings":
                nextMenu = new SettingsMenu();
                break;
            case "network":
                nextMenu = new NetworkMenu();
                break;
            case "news":
                nextMenu = new NewsMenu();
                break;
            case "profile":
                nextMenu = new ProfileMenu();
                break;
            case "collection":
                nextMenu=new CollectionMenu();
                break;
            case "greenHouse":
                nextMenu=new GreenHouseMenu();
                break;
            case "travelLog":
                nextMenu=new QuestMenu();
                break;
            case "leaderboard":
                nextMenu=new LeaderBoardMenu();
                break;
            default:
                nextMenu = new MainMenu();
                return new Result("You cannot enter this menu." , nextMenu);
        }

        return new Result("Enterned " + nextMenu.getName() , nextMenu);

    }
    public Result exit(Matcher matcher) {
        return new Result("Use 'menu logout' to exit from Main Menu.", new MainMenu());
    }
    public Result logout(Matcher matcher) {
        SaveManager.getInstance().delete("session.json");
        return new Result("logged out", new RegisterMenu());
    }
}
