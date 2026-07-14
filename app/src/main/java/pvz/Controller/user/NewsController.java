package pvz.Controller.user;

import java.util.ArrayList;
import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.User.Message;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.Result;

public class NewsController {
    public Result showUnread(Matcher matcher) {
        ArrayList<Message> messages = AppContext.getInstance().getCurrentUser().getNews().getMessages();
        StringBuilder result = new StringBuilder();
        if(messages.isEmpty()) {
            return new Result("No messages found.");
        }
        if(messages.stream().noneMatch(Message::isUnread)) {
            return new Result("No unread messages found.");
        }
        for(Message message : messages){
            if(message.isUnread()){
                result.append(message + "\n");
            }
        }
        return new Result(result.toString()); 
    }
    public Result showAll(Matcher matcher) { 
        ArrayList<Message> messages = AppContext.getInstance().getCurrentUser().getNews().getMessages();
        StringBuilder result = new StringBuilder();
        if(messages.isEmpty()) {
            return new Result("No messages found.");
        }
        for(Message message : messages){
            result.append(message + "\n");
        }
        return new Result(result.toString());    
    }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
