package View;

import Controller.NetworkController;
import Models.Commands.NetworkMenuCommand;

import java.util.regex.Matcher;

public class NetworkMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = NetworkMenuCommand.CONNECT.getMatcher(input)) != null) return NetworkController.connect(matcher);
        if ((matcher = NetworkMenuCommand.BACK.getMatcher(input)) != null) return NetworkController.back(matcher);
        return new Result("Invalid command in Network Menu.", this);
    }
}
