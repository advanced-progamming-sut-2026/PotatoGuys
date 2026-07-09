package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.user.RegisterController;
import pvz.Enums.Commands.RegisterMenuCommand;

public class PickSecurityQuestionMenu implements Menu {
    private final RegisterController controller;

    public PickSecurityQuestionMenu(RegisterController controller) {
        this.controller = controller;
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        
        if ((matcher = RegisterMenuCommand.PICK_QUESTION.getMatcher(input)) != null) return controller.pickQuestion(matcher);
        if ((matcher = RegisterMenuCommand.EXIT.getMatcher(input)) != null) return new Result("Exited to Register Menu.", new RegisterMenu());
        
        return new Result("Invalid command. Please pick the security question or type 'exit'.", this);
    }

    @Override
    public String getName() {
        return "Pick Security Question menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}