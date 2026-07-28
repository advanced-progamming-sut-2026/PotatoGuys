package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.user.LoginController;
import pvz.enums.commands.LoginMenuCommand;

public class ResetPasswordMenu implements Menu {
    private final LoginController controller;

    public ResetPasswordMenu(LoginController controller) {
        this.controller = controller;
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;

        if ((matcher = LoginMenuCommand.RESET_PASSWORD.getMatcher(input)) != null)
            return controller.resetPassword(matcher);
        if ((matcher = LoginMenuCommand.EXIT.getMatcher(input)) != null)
            return new Result("Exited to Login Menu.", new LoginMenu());
        if ((matcher = LoginMenuCommand.HELP.getMatcher(input)) != null)
            return new Result(LoginMenuCommand.getHelp());
        return new Result("Invalid command. Please reset password or type 'exit'.", this);
    }

    @Override
    public String getName() {
        return "Forgot Password menu";
    }

    @Override
    public Result onEnter() {
        return new Result("User reset password -p <newPassword> to change password!\n");
    }
}