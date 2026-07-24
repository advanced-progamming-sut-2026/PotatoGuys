package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.MainController;
import pvz.enums.commands.MainMenuCommand;
import pvz.models.AppContext;
import pvz.models.user.Message;
import pvz.models.user.User;

public class MainMenu implements Menu {
    MainController controller = new MainController();

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = MainMenuCommand.ENTER_MENU.getMatcher(input)) != null)
            return controller.enterMenu(matcher);
        if ((matcher = MainMenuCommand.EXIT.getMatcher(input)) != null)
            return controller.exit(matcher);
        if ((matcher = MainMenuCommand.LOGOUT.getMatcher(input)) != null)
            return controller.logout(matcher);
        if ((matcher = MainMenuCommand.HELP.getMatcher(input)) != null)
            return new Result(MainMenuCommand.getHelp());
        return new Result("Invalid command in Main Menu.", this);
    }

    @Override
    public String getName() {
        return "Main menu";
    }

    @Override
    public Result onEnter() {
        User currentUser = AppContext.getInstance().getCurrentUser();
        boolean hasUnread = false;

        if (currentUser != null && currentUser.getProfile() != null && currentUser.getProfile().getNews() != null) {
            hasUnread = currentUser.getProfile().getNews().getMessages().stream()
                    .anyMatch(Message::isUnread);
        }

        // Red ANSI text with (!) badge if unread news exists
        String newsBadge = hasUnread ? "\u001B[31m(!)\u001B[0m" : "";

        StringBuilder sb = new StringBuilder("Entered Main Menu.");
        if (hasUnread) {
            sb.append("\nYou have unread news! ").append(newsBadge)
                    .append(" Type 'menu news show-unread' in news menu.");
        }

        return new Result(sb.toString());
    }
}