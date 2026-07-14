package pvz.Controller.user;

import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Utils.SaveManager;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.Result;

public class SettingsController {
    public Result changeDifficulty(Matcher matcher) { 
        int newDifficulty = Integer.parseInt(matcher.group("difficulty"));
        if(newDifficulty < 1 || newDifficulty > 5) {
            return new Result("difficulty must be between 1 and 5");
        }
        AppContext.getInstance().getCurrentUser().getSetting().setDifficulty(newDifficulty);
        SaveManager.getInstance().save(AppContext.getInstance().getCurrentUser() , "users/" + AppContext.getInstance().getCurrentUser().getId() + ".json");
        return new Result("changed difficulty to " + newDifficulty);  
    }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
