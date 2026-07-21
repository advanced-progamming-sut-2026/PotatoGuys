package pvz.controller.user;

import java.util.ArrayList;
import java.util.regex.Matcher;

import pvz.models.AppContext;
import pvz.models.user.Message;
import pvz.view.MainMenu;
import pvz.view.Menu;
import pvz.view.Result;

public class NewsController {
    public Result showUnread(Matcher matcher) {
        ArrayList<Message> messages = AppContext.getInstance().getCurrentUser().getProfile().getNews().getMessages();
        StringBuilder result = new StringBuilder();
        if(messages.isEmpty()) {
            return new Result("No messages found.");
        }
        if(messages.stream().noneMatch(Message::isUnread)) {
            return new Result("No unread messages found.");
        }
        for(Message message : messages){
            if(message.isUnread()){
                result.append(message.getMessage() + "\n");
                message.setUnread(false);
            }
        }
        return new Result(result.toString()); 
    }
    public Result showAll(Matcher matcher) { 
        ArrayList<Message> messages = AppContext.getInstance().getCurrentUser().getProfile().getNews().getMessages();
        StringBuilder result = new StringBuilder();
        if(messages.isEmpty()) {
            return new Result("No messages found.");
        }
        for(Message message : messages){
            result.append(message.getMessage() + "\n");
            message.setUnread(false);
        }
        return new Result(result.toString());    
    }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
