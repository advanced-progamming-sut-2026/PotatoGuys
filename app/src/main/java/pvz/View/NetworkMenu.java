package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.NetworkController;
import pvz.Models.Commands.NetworkMenuCommand;

public class NetworkMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        NetworkController controller=new NetworkController();
        Matcher matcher;
        if ((matcher = NetworkMenuCommand.CONNECT.getMatcher(input)) != null) return controller.connect(matcher);
        if ((matcher = NetworkMenuCommand.BACK.getMatcher(input)) != null) return controller.back(matcher);
        return new Result("Invalid command in Network Menu.", this);
    }

    @Override
    public String getName(){
        return "Network menu";
    }
}
