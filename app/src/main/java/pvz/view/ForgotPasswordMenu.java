package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.user.LoginController;
import pvz.enums.commands.LoginMenuCommand;
import pvz.models.user.User;

public class ForgotPasswordMenu implements Menu {
    private final LoginController controller;

    public ForgotPasswordMenu(User user) {
        this.controller = new LoginController();
        controller.currentUser = user;
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;

        if ((matcher = LoginMenuCommand.ANSWER.getMatcher(input)) != null)
            return controller.answer(matcher);
        if ((matcher = LoginMenuCommand.EXIT.getMatcher(input)) != null)
            return new Result("Exited to Login Menu.", new LoginMenu());
        if ((matcher = LoginMenuCommand.HELP.getMatcher(input)) != null)
            return new Result(LoginMenuCommand.getHelp());
        return new Result("Invalid command. Please answer the security question or type 'exit'.", this);
    }

    @Override
    public String getName() {
        return "Forgot Password menu";
    }

    @Override
    public Result onEnter() {
        return new Result("Use answer -a <answer> to answer the question!\n");
    }
}