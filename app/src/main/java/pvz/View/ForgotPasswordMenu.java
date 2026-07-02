package pvz.View;

import java.util.regex.Matcher;
import pvz.Controller.LoginController;
import pvz.Enums.Commands.LoginMenuCommand;

public class ForgotPasswordMenu implements Menu {
    private final LoginController controller;

    public ForgotPasswordMenu(LoginController controller) {
        this.controller = controller;
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        
        if ((matcher = LoginMenuCommand.ANSWER.getMatcher(input)) != null) return controller.answer(matcher);
        if ((matcher = LoginMenuCommand.EXIT.getMatcher(input)) != null) return new Result("Exited to Login Menu.", new LoginMenu());
        
        return new Result("Invalid command. Please answer the security question or type 'exit'.", this);
    }

    @Override
    public String getName() {
        return "Forgot Password menu";
    }
}