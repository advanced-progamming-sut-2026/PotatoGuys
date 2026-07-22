package pvz.controller.user;

import java.util.ArrayList;
import java.util.regex.Matcher;

import pvz.models.AppContext;
import pvz.models.user.Message;
import pvz.models.user.User;
import pvz.view.MainMenu;
import pvz.view.Menu;
import pvz.view.Result;

public class NewsController {
    public Result showUnread(Matcher matcher) {
        User user = AppContext.getInstance().getCurrentUser();
        ArrayList<Message> messages = user.getProfile().getNews().getMessages();
        StringBuilder result = new StringBuilder();

        if (messages == null || messages.isEmpty()) {
            return new Result("No messages found.");
        }

        boolean foundUnread = false;
        for (Message message : messages) {
            if (message.isUnread()) {
                foundUnread = true;
                result.append("- ").append(message.getMessage()).append("\n");
                message.setUnread(false); // Mark as read
            }
        }

        if (!foundUnread) {
            return new Result("No unread messages found.");
        }

        user.saveUser(); // Save changes to JSON
        return new Result("=== UNREAD NEWS ===\n" + result.toString().trim());
    }

    public Result showAll(Matcher matcher) {
        User user = AppContext.getInstance().getCurrentUser();
        ArrayList<Message> messages = user.getProfile().getNews().getMessages();
        StringBuilder result = new StringBuilder();

        if (messages == null || messages.isEmpty()) {
            return new Result("No messages found.");
        }

        for (Message message : messages) {
            String status = message.isUnread() ? "\u001B[31m[NEW]\u001B[0m " : "[READ] ";
            result.append(status).append("- ").append(message.getMessage()).append("\n");
            message.setUnread(false); // Mark as read
        }

        user.saveUser(); // Save changes to JSON
        return new Result("=== ALL NEWS ===\n" + result.toString().trim());
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}