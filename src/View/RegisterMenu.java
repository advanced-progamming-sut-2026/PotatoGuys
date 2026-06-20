package View;

import Controller.RegisterController;
import Models.Commands.RegisterMenuCommand;

import java.util.regex.Matcher;

public class RegisterMenu implements Menu {
    RegisterController controller=new RegisterController();

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = RegisterMenuCommand.REGISTER.getMatcher(input)) != null) return controller.register(matcher);
        if ((matcher = RegisterMenuCommand.PICK_QUESTION.getMatcher(input)) != null) return controller.pickQuestion(matcher);
        if ((matcher = RegisterMenuCommand.ENTER_LOGIN.getMatcher(input)) != null) return controller.enterLogin(matcher);
        if ((matcher = RegisterMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Register Menu.", this);
    }
}
