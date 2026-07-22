package pvz.controller;

import java.util.regex.Matcher;

import pvz.utils.SaveManager;
import pvz.view.CollectionMenu;
import pvz.view.GreenHouseMenu;
import pvz.view.LeaderBoardMenu;
import pvz.view.MainMenu;
import pvz.view.Menu;
import pvz.view.NetworkMenu;
import pvz.view.NewsMenu;
import pvz.view.ProfileMenu;
import pvz.view.QuestMenu;
import pvz.view.RegisterMenu;
import pvz.view.Result;
import pvz.view.SettingsMenu;
import pvz.view.game.GameMenu;
import pvz.view.game.modals.GameModesModal;

public class MainController {
    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "game":
                nextMenu = new GameMenu(new GameModesModal());
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
