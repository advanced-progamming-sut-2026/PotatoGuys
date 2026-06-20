package View;

import Controller.LoginController;
import Models.Commands.LoginMenuCommand;

import java.util.regex.Matcher;

public class LoginMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = LoginMenuCommand.LOGIN.getMatcher(input)) != null) return LoginController.login(matcher);
        if ((matcher = LoginMenuCommand.FORGET_PASSWORD.getMatcher(input)) != null) return LoginController.forgetPassword(matcher);
        if ((matcher = LoginMenuCommand.ANSWER.getMatcher(input)) != null) return LoginController.answer(matcher);
        if ((matcher = LoginMenuCommand.EXIT.getMatcher(input)) != null) return LoginController.exit(matcher);
        return new Result("Invalid command in Login Menu.", this);
    }
}
