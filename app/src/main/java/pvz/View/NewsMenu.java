package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.user.NewsController;
import pvz.enums.commands.NewsMenuCommand;

public class NewsMenu implements Menu {
    NewsController controller=new NewsController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = NewsMenuCommand.SHOW_UNREAD.getMatcher(input)) != null) return controller.showUnread(matcher);
        if ((matcher = NewsMenuCommand.SHOW_ALL.getMatcher(input)) != null) return controller.showAll(matcher);
        if ((matcher = NewsMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = NewsMenuCommand.HELP.getMatcher(input)) != null) return new Result(NewsMenuCommand.getHelp());
        return new Result("Invalid command in News Menu.", this);
    }

    @Override
    public String getName(){
        return "News menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
