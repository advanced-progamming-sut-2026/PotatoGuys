package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.RegisterController;
import pvz.Enums.Commands.RegisterMenuCommand;

public class RegisterMenu implements Menu {
    RegisterController controller=new RegisterController();

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = RegisterMenuCommand.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = RegisterMenuCommand.REGISTER.getMatcher(input)) != null) return controller.register(matcher);
        if ((matcher = RegisterMenuCommand.PICK_QUESTION.getMatcher(input)) != null) return controller.pickQuestion(matcher);
        if ((matcher = RegisterMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Register Menu.", this);
    }

    @Override
    public String getName(){
        return "Register menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
