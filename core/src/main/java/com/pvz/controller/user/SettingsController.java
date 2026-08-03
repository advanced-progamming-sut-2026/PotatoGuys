package com.pvz.controller.user;

import java.util.regex.Matcher;

import com.pvz.models.AppContext;
import com.pvz.utils.SaveManager;

public class SettingsController {
/*
    public Result changeDifficulty(Matcher matcher) {
        int newDifficulty = Integer.parseInt(matcher.group("difficulty"));
        if (newDifficulty < 1 || newDifficulty > 5) {
            return new Result("difficulty must be between 1 and 5");
        }
        AppContext.getInstance().getCurrentUser().getSetting().setDifficulty(newDifficulty);
        SaveManager.getInstance().save(AppContext.getInstance().getCurrentUser(),
                "users/" + AppContext.getInstance().getCurrentUser().getId() + ".json");
        return new Result("changed difficulty to " + newDifficulty);
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new pvz.view.OldMainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
*/
}
