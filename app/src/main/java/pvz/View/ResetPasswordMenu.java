package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.LoginController;
import pvz.Enums.Commands.LoginMenuCommand;

public class ResetPasswordMenu implements Menu {
    private final LoginController controller;

    public ResetPasswordMenu(LoginController controller) {
        this.controller = controller;
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        
        if ((matcher = LoginMenuCommand.RESET_PASSWORD.getMatcher(input)) != null) return controller.resetPassword(matcher);
        if ((matcher = LoginMenuCommand.EXIT.getMatcher(input)) != null) return new Result("Exited to Login Menu.", new LoginMenu());
        
        return new Result("Invalid command. Please reset password or type 'exit'.", this);
    }

    @Override
    public String getName() {
        return "Forgot Password menu";
    }
}